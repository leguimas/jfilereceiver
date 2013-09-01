package jfilereceiver.files;

import java.io.*;
import java.util.*;
import java.sql.SQLException;
import jfilereceiver.conf.*;
import jfilereceiver.general.*;

/**
 * Title:        UpFile
 * Description:  Classe que realiza o processamento dos arquivos UP*.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Gordo&#153;
 * @version      1.0
 */

public class UpFile extends GeneralFile {

  // constantes
  static final String ID_APPLICATION = "UPFILE";
  static final String DETAIL_REGISTRY = "1";

  // constantes sobre os erros de banco de dados
  static final int JFR_ERROR_IRREGULAR_FILE = 1;
  static final int JFR_ERROR_DUPLICATE_FILE = 56;
  static final int JFR_SYSTEM_ERROR = 69;
  static final int JFR_ERROR_UNIQUE_KEY = 99;

  /**
   * Método responsável por realizar a inserção dos dados em FILA_RECEPCAO.
   *
   * @author Gordo&#153;
   * @param  _txtVersion informações sobre a versão do arquivo que está sendo manipulado
   * @param  _values valores a serem inseridos
   * @param  _tipIrrCodigo tipo da irregularidade
   */
  public void newUpRegistry (TxtVersion _txtVersion, LinkedList _values, String _tipIrrCodigo,
                             String _upFileType, String _version) throws Exception, SQLException {
    // montando a query a ser executada
    String query = new String("");
    query = "insert into FILA_RECEPCAO\n" +
            "  (FIL_RECCODIGO,\n" +
            "   FIL_RECTIPOARQUIVO,\n" +
            "   FIL_RECDATAENTRADAFILA,\n" +
            "   FIL_RECVERSAO,\n" +
            "   FIL_RECSTSPROCESSANDO,\n" +
            databaseTools.getStringColumns(_txtVersion) + ")\n" +
            "  values (" + util.NewId.get(newIdUrl, this.ID_APPLICATION) + ",\n" +
            "           '" + _upFileType + "',\n" +
            "           getDate(),\n" +
            "           '" + _version + "',\n " +
            "           'N',\n" +
            databaseTools.getStringColumns(_txtVersion, _values) + ")";
    // obtendo as informações sobre a conexão para realizar a transação
    int connHandler = this.connectionPool.getConnectionHandler();
    this.execute(query, connHandler);
    // commita a operação
    this.execute("begin transaction commit", connHandler);
    // libera a conexão utilizada
    this.connectionPool.freeConnection(connHandler);
  }

  /**
   * Método responsável por realizar a inserção dos dados em FILA_RECEPCAO_IRREGULAR.
   *
   * @author Gordo&#153;
   * @param _irrCodigo codigo da irregularidade
   * @param _upFileType tipo do arquivo em questão
   * @param _version versão utilizada
   * @param _uniCodigo codigo da unidade
   * @param _parte1 fil_recirrparte1
   * @param _parte2 fil_recirrparte2
   */
  public void irregularUp (int _irrCodigo, String _upFileType, String _version,
                           String _uniCodigo, String _parte1, String _parte2) throws Exception, SQLException {
    // montando a query a ser executada
    String query = new String("");
    query = "insert into FILA_RECEPCAO_IRREGULAR\n" +
            "  (FIL_RECIRRCODIGO,\n" +
            "   UNI_CODIGO,\n" +
            "   TIP_IRRCODIGO,\n" +
            "   FIL_RECIRRDATAENTRADAFILA,\n" +
            "   FIL_RECIRRCODIGOIRR,\n" +
            "   FIL_RECIRRTIPOARQUIVO,\n" +
            "   FIL_RECIRRDATA,\n" +
            "   FIL_RECIRRPARTE1,\n" +
            "   FIL_RECIRRPARTE2,\n" +
            "   FIL_RECIRRVERSAO,\n" +
            "   FIL_RECIRRREPROCESSAR)\n" +
            "  values (" + util.NewId.get(newIdUrl, this.ID_APPLICATION) + ",\n" +
            "           '" + _uniCodigo + "',\n" +
            "           " + _irrCodigo + ",\n" +
            "           getDate(),\n" +
            "           '',\n" +
            "           '" + _upFileType + "',\n" +
            "           getDate(),\n" +
            "           '" + _parte1 + "',\n" +
            "           '" + _parte2 + "',\n" +
            "           '" + _version + "',\n " +
            "           'N')";
    // obtendo as informações sobre a conexão para realizar a transação
    int connHandler = this.connectionPool.getConnectionHandler();
    this.execute(query, connHandler);
    // commita a operação
    this.execute("begin transaction commit", connHandler);
    // libera a conexão utilizada
    this.connectionPool.freeConnection(connHandler);
  }

  /**
   * Método responsável por obter o código da irregularidade.
   *
   * @author Gordo&#153;
   * @param  _textException texto da exceção
   * @return código da irregularidade
   */
  public int getIrregularCode (Exception _exception) {
    int result = JFR_ERROR_IRREGULAR_FILE;
    // verifica se foi um problema de SQL
    if (_exception instanceof java.sql.SQLException) {
      // verifica se o erro foi o de duplicate key
      if (_exception.toString().toUpperCase().indexOf("IX_FILA_RECEPCAO_1") >= 0)
        result = JFR_ERROR_UNIQUE_KEY;
      else if (_exception.toString().indexOf("duplicate key") >= 0)
        result = JFR_ERROR_DUPLICATE_FILE;
      else if (_exception.toString().indexOf("foreign key") >= 0)
        result = -1;
      else if ((_exception.toString().indexOf("deadlock") >= 0) || (_exception.toString().indexOf("timeout") >= 0) ||
               (_exception.toString().indexOf("permission denied") >= 0))
        result = JFR_SYSTEM_ERROR;
    }
    else
      result = JFR_SYSTEM_ERROR;
    return result;
  }

  /**
   * Método run da Thread. Fica realizando processamento de arquivos UP. O processamento
   * do arquivo consiste em coletar as informações do mesmo e colocá-las em um
   * banco de dados. Caso a operação ocorra com sucesso, o arquivo é movido para
   * o diretório processedDir. Caso ocorra algum erro durante o processamento, o
   * arquivo é movido para a pasta irregularDir. A thread fica sendo executada
   * ate a propriedade stop ser igual a true.
   *
   * @author Gordo&#153;
   */
  public void run () {
    // variável utilizada para leitura do arquivo
    BufferedReader fileReader = null;
    // setando o log para o processamento deste tipo de arquivo
    this.setLogProcess();
    logProcess.log("[jFileReceiver.files.UpFile] Startada uma thread para processamento " + this.getFilePattern() + ".", logProcess.logGenerator.STATUS);
    boolean getOpen = false;
    irregular = false;
    // enquanto a Thread não for setada para parar
    while (! stop) {
      // se a thread estiver ativa
      if (this.active) {
        logProcess.log("[jFileReceiver.files.UpFile] Iniciado o processamento do arquivo " + file2Process.getPath() + ".", logProcess.logGenerator.STATUS);
        // variável que armazena as linhas que apresentaram algum problema
        LinkedList errorLines = new LinkedList();
        // variável que armazena as linhas com problemas de versão
        LinkedList versionError = new LinkedList();
        this.errorProcess = false;
        // tenta abrir o arquivo para leitura
        try {
          fileReader = new BufferedReader (new FileReader (file2Process));
          // seta variável indicando sucesso na abertura do arquivo
          getOpen = true;
        }
        catch (Exception fileException) {
          try {
            logProcess.log("[jFileReceiver.files.UpFile] Erro na abertura do arquivo " + file2Process.getPath() + ". " + fileException.toString() + ". O arquivo será colocado na pasta de arquivos irregulares: " + validFile.getIrregularHomeOutBoxDir(), logProcess.logGenerator.ERROR);
            logApplication.log("[jFileReceiver.files.UpFile] Erro na abertura do arquivo " + file2Process.getPath() + ". " + fileException.toString() + ". O arquivo será colocado na pasta de arquivos irregulares: " + validFile.getIrregularHomeOutBoxDir(), logApplication.logGenerator.ERROR);
          }
          catch (Exception ex) {
          }
          // seta variável indicando falha na abertura do arquivo
          getOpen = false;
        }
        // se não ocorreu problemas na abertura do arquivo
        if (getOpen) {
          // obtém o tipo do arquivo UP
          // as 3 primeiras letras do nome do arquivo, identificam o tipo do mesmo. Como o arquivo está
          // lockado (com um _ no começo) pega-se os caracteres da posição 1 até a posição 3
          try {
            String upFileType = file2Process.getName().substring(1, 4).toUpperCase();
            int indexLine = 1;
            // percorrendo todas as linhas do arquivo texto
            for (String line = fileReader.readLine(); line != null; line = fileReader.readLine()) {
              try {
                // verifica se a linha atual é uma linha de detalhes
                if (DETAIL_REGISTRY.equals(line.substring(0,1))) {
                  // obtendo a versão do arquivo a qual essa linha se refere
                  int indexVersion = this.getVersion(line.length());
                  if (indexVersion < 0) {
                    versionError.add(line);
                    throw new Exception ("Não foi encontrada nenhuma versão de arquivo " + validFile.getFilePattern() + " para a linhas com tamanho " + line.length() + ".");
                  }
                  try {
                    // Obtém as informações sobre a versão encontrada
                    FileVersion fileVersion = validFile.getFileVersions(indexVersion);
                    TxtVersion txtVersion = fileVersion.getTxtVersion();
                    LinkedList fieldValues = new LinkedList();
                    TxtFieldInformation fieldInformation = new TxtFieldInformation();
                    // variáveis para armazenar informações no caso de irregularidade
                    String uniCodigo = new String("");
                    String filRecParte1 = new String("");
                    String filRecParte2 = new String("");
                    // percorre as informações do vetor para obter os valores contidos na linha do arquivo
                    for (int indexField = 0; indexField < txtVersion.getFieldsInformationSize(); indexField ++) {
                      fieldInformation = txtVersion.getTxtFieldInformation(indexField);
                      fieldValues.add(indexField, line.substring(fieldInformation.getInitialPosition(), fieldInformation.getFinalPosition() + 1).trim());
                      if ("UNI_CODIGO".equals(fieldInformation.getDatabaseColumn().toUpperCase()))
                        uniCodigo = line.substring(fieldInformation.getInitialPosition(), fieldInformation.getFinalPosition() + 1).trim();
                      else if ("FIL_RECPARTE1".equals(fieldInformation.getDatabaseColumn().toUpperCase()))
                        filRecParte1 = line.substring(fieldInformation.getInitialPosition(), fieldInformation.getFinalPosition() + 1).trim();
                      else if ("FIL_RECPARTE2".equals(fieldInformation.getDatabaseColumn().toUpperCase()))
                        filRecParte2 = line.substring(fieldInformation.getInitialPosition(), fieldInformation.getFinalPosition() + 1).trim();
                    }
                    // realiza a inserção das informações no banco
                    try {
                      this.newUpRegistry(txtVersion, fieldValues, "null", upFileType, fileVersion.getCodeVersion());
                      irregular = false;
                      // colocando o arquivo no arquivo de saída de linhas processadas
                      this.outputLine(line, true, indexLine);
                    }
                    // tratando algum problema de SQL
                    catch (Exception sqlException) {
                      irregular = true;
                      int irrCodigo = this.getIrregularCode(sqlException);
                      // registra a irregularidade
                      if (irrCodigo > 0) {
                        try {
                          // verifica se é algum erro do sistema
                          if (irrCodigo == JFR_SYSTEM_ERROR) {
                            errorLines.add(line);
                            logProcess.log("[jFileReceiver.files.UpFile] Devido a problemas no sistema ao se processar a linha " + indexLine + " do arquivo " + file2Process.getPath() + " a mesma sera conservada neste arquivo para ser processada futuramente. " + sqlException.toString() + ".", logProcess.logGenerator.ERROR);
                          }
                          else if (irrCodigo == JFR_ERROR_UNIQUE_KEY) {
                            this.outputLine(line, true, indexLine);
                          }
                          else {
                            // loga as mensagens
                            logProcess.log("[jFileReceiver.files.UpFile] A linha " + indexLine + " do arquivo " + file2Process.getPath() + " está irregular. Inserindo em FILA_RECEPCAO_IRREGULAR. " + sqlException.toString() + ".", logProcess.logGenerator.ERROR);
                            logApplication.log("[jFileReceiver.files.UpFile] A linha " + indexLine + " do arquivo " + file2Process.getPath() + " está irregular. Inserindo em FILA_RECEPCAO_IRREGULAR. " + sqlException.toString() + ".", logApplication.logGenerator.ERROR);
                            // coloca a linha no arquivo de linhs irregulares
                            this.outputLine(line, false, indexLine);
                            // tentando inserir em FILA_RECEPCAO_IRREGULAR
                            this.irregularUp(irrCodigo, upFileType, fileVersion.getCodeVersion(), uniCodigo, filRecParte1, filRecParte2);
                          }
                        }
                        // tratando problemas ao se registrar a irregularidade da linha
                        catch (Exception irrException) {
                          // veirifica se a exceção foi causada por algum problema do sistema
                          irrCodigo = this.getIrregularCode(irrException);
                          if (irrCodigo == JFR_SYSTEM_ERROR)
                            errorLines.add(line);
                          else {
                            // loga a informação
                            logProcess.log("[jFileReceiver.files.UpFile] Erro na tentativa de se gravar a irregularidade do arquivo " + file2Process.getPath() + ". " + irrException.toString() + ". Esta linha (" + indexLine + ") está sendo colocada no arquivo de linhas irregulares.", logProcess.logGenerator.ERROR);
                            logApplication.log("[jFileReceiver.files.UpFile] Erro na tentativa de se gravar a irregularidade do arquivo " + file2Process.getPath() + ". " + irrException.toString() + ". Esta linha (" + indexLine + ") está sendo colocada no arquivo de linhas irregulares.", logApplication.logGenerator.ERROR);
                          }
                        }
                      }
                      // caso a irregularidade seja problema de FK
                      else {
                        logProcess.log("[jFileReceiver.files.UpFile] As informações da linha " + indexLine + " contida no arquivo " + file2Process.getPath() + " estão irregulares. " + sqlException.toString() + ". Esta linha está sendo colocada no arquivo de linhas irregulares.", logProcess.logGenerator.ERROR);
                        logApplication.log("[jFileReceiver.files.UpFile] As informações da linha " + indexLine + " contida no arquivo " + file2Process.getPath() + " estão irregulares. " + sqlException.toString() + ". Esta linha está sendo colocada no arquivo de linhas irregulares.", logApplication.logGenerator.ERROR);
                        this.outputLine(line, false, indexLine);
                      }
                    }
                  }
                  catch (Exception fileProcessException) {
                    irregular = true;
                    logProcess.log("[jFileReceiver.files.UpFile] Erro no processamento da linha " + indexLine + " do arquivo " +  file2Process.getPath() + ". " + fileProcessException.toString(), logProcess.logGenerator.ERROR);
                    logApplication.log("[jFileReceiver.files.UpFile] Erro no processamento da linha " + indexLine + " do arquivo " +  file2Process.getPath() + ". " + fileProcessException.toString(), logApplication.logGenerator.ERROR);
                    this.outputLine(line, false, indexLine);
                  }
                }
                indexLine ++;
              }
              catch (Exception versionException) {
                irregular = true;
                logProcess.log("[jFileReceiver.files.UpFile] Erro na obtenção da versão para a linha " + indexLine + " do arquivo " +  file2Process.getPath() + ". " + versionException.toString(), logProcess.logGenerator.ERROR);
                logApplication.log("[jFileReceiver.files.UpFile] Erro na obtenção da versão para a linha " + indexLine + " do arquivo " +  file2Process.getPath() + ". " + versionException.toString(), logApplication.logGenerator.ERROR);
              }
            }
            // armazena o nome do arquivo
            String originalFile = file2Process.getPath();
            String originalName = file2Process.getName();
            // deslocka o arquivo que foi processado
            try {
              fileReader.close();
              // finaliza o processamento do arquivo
              this.endProcess(irregular, errorProcess);
              // cria o arquivo com as linhas problemáticas
              BufferedWriter fileErrorLines = null;
              for (int indexFile = 0; indexFile < errorLines.size(); indexFile ++) {
                if (indexFile == 0)
                  fileErrorLines = new BufferedWriter(new FileWriter(originalFile));
                fileErrorLines.write((String) errorLines.get(indexFile));
                fileErrorLines.newLine();
              }
              if (fileErrorLines != null) {
                // fecha o arquivo e o deslocka
                fileErrorLines.flush();
                fileErrorLines.close();
                fileTools.unlockFile(new File(originalFile), false, false, validFile.getOverwriteFiles());
              }
            }
            catch (Exception unlockException) {
              logProcess.log("[jFileReceiver.files.UpFile] Problemas na manipulação do arquivo " + file2Process.getPath() + ". " + unlockException.toString() + ". Caso o arquivo esteja lockado, desloque-o e coloque no diretório " + validFile.getProcessedHomeOutBoxDir() + " e uma cópia no diretório " + validFile.getIrregularHomeOutBoxDir() + ".", logProcess.logGenerator.ERROR);
              logApplication.log("[jFileReceiver.files.UpFile] Problemas na manipulação do arquivo " + file2Process.getPath() + ". " + unlockException.toString() + ". Caso o arquivo esteja lockado, desloque-o e coloque no diretório " + validFile.getProcessedHomeOutBoxDir() + " e uma cópia no diretório " + validFile.getIrregularHomeOutBoxDir() + ".", logApplication.logGenerator.ERROR);
            }
            try {
              // cria o arquivo com as linhas com problemas em versão
              BufferedWriter versionErrorLines = null;
              for (int indexFile = 0; indexFile < versionError.size(); indexFile ++) {
                if (indexFile == 0)
                  versionErrorLines = new BufferedWriter(new FileWriter(this.rejectedDir + originalName));
                versionErrorLines.write((String) versionError.get(indexFile));
                versionErrorLines.newLine();
              }
              if (versionErrorLines != null) {
                // fecha o arquivo e o deslocka
                versionErrorLines.flush();
                versionErrorLines.close();
                fileTools.unlockFile(new File(this.rejectedDir + originalName), false, true, validFile.getOverwriteFiles());
              }
            }
            catch (Exception unlockException) {
              logProcess.log("[jFileReceiver.files.UpFile] Problemas na manipulação do arquivo " + file2Process.getPath() + ". " + unlockException.toString() + ". Caso o arquivo esteja lockado, desloque-o e coloque no diretório " + validFile.getProcessedHomeOutBoxDir() + " e uma cópia no diretório " + validFile.getIrregularHomeOutBoxDir() + ".", logProcess.logGenerator.ERROR);
              logApplication.log("[jFileReceiver.files.UpFile] Problemas na manipulação do arquivo " + file2Process.getPath() + ". " + unlockException.toString() + ". Caso o arquivo esteja lockado, desloque-o e coloque no diretório " + validFile.getProcessedHomeOutBoxDir() + " e uma cópia no diretório " + validFile.getIrregularHomeOutBoxDir() + ".", logApplication.logGenerator.ERROR);
            }
          }
          catch (Exception stringException) {
            logProcess.log("[jFileReceiver.files.UpFile] Erro na obtenção do tipo do arquivo " + file2Process.getPath() + ". " + stringException.toString() + ". O mesmo será deslockado.", logProcess.logGenerator.ERROR);
            this.endProcess(true, false);
          }
        }
        // caso ocorreu algum problema, move o arquivo para o diretorio de arquivos irregulares
        else {
          this.endProcess(true, false);
        }
        // seta o horário do último acesso a esta thread
        this.lastAccess.setTime(System.currentTimeMillis());
        // pausa a thread para que outro processo possa usá-lo
        this.active = false;
        this.errorProcess = false;
        this.irregular = false;
      }
      // sleep para evitar 100% de CPU
      try {
        Thread.sleep(this.sleepTime);
      }
      catch (Exception sleepException) {
        logApplication.log("[jFileReceiver.files.UpFile] Problemas no sleep da thread UpFile: " + sleepException.toString(), logApplication.logGenerator.ATTENTION);
        logProcess.log("[jFileReceiver.files.UpFile] Problemas no sleep da thread UpFile: " + sleepException.toString(), logApplication.logGenerator.ATTENTION);
      }
    }
    logProcess.log("[jFileReceiver.files.UpFile] Finalizada uma thread para processamento " + this.getFilePattern() + ".", logApplication.logGenerator.STATUS);
  }

}