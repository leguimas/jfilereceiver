package jfilereceiver.files;

import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.sql.*;
import util.ConnectionPool;
import jfilereceiver.conf.*;
import jfilereceiver.log.*;
import jfilereceiver.general.*;

/**
 * Title:        GeneralFile
 * Description:  Classe que implementa a interface FileInterface e herda os métodos da
 *               classe java.lang.Thread. Representa um arquivo qualquer que é processado
 *               pelo jFileReceiver.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class GeneralFile extends java.lang.Thread implements jfilereceiver.files.FileInterface {

  // constantes
  public static final int MAX_TRY = 10;

  // número máximo de tentativas para realizar uma operação no banco de dados
  static final int AMOUNT_TRY = 3;

  // variáveis
  public ConnectionPool connectionPool;
  public LogMessages logApplication;
  public LogMessages logProcess;
  public File file2Process;
  public ValidFile validFile;
  public String processedDir = new String("");
  public String irregularDir = new String("");
  public String rejectedDir = new String("");
  public String newIdUrl = new String("");
  public String filePattern = new String("");
  public java.util.Date lastAccess = new java.util.Date(System.currentTimeMillis());
  public FileTools fileTools = new FileTools();
  public DatabaseTools databaseTools = new DatabaseTools();
  public int idleTime;
  public int sleepTime;
  public int logLevel;
  // variável que indicará se a thread está ativa ou não. Seu valor é diretamente influenciado
  // pelo método setPauseThread()
  public boolean active = false;
  // variável que indicará se a thread deve parar ou não. Seu valor é diretamente influenciado
  // pelo método setStopThread()
  public boolean stop = false;
  //
  public boolean errorProcess = false;
  public boolean irregular = false;

  /**
   * Método responsável por iniciar uma thread. Basicamente é para fazer um
   * this.start na classe para que o método run() possa ser "acionado".
   *
   * @author Gordo&#153;
   */
  public void startThread() {
    this.start();
  }

  /**
   * Método responsável por finalizar uma thread. Seta a propriedade que para o
   * loop principal do método run().
   *
   * @author Gordo&#153;
   */
  public void stopThread() {
    this.stop = true;
  }

  /**
   * Método responsável por pausar ou despausar uma thread. Seta a propriedade
   * que indica se a thread está ativa ou não de acordo com o valor de _pause.
   *
   * @author Gordo&#153;
   * @param  _pause true caso deseje pausar a thread, false caso contrário
   */
  public void pauseThread(boolean _pause) {
    this.active = ! _pause;
  }

  /**
   * Seta a propriedade que indica qual é o nível de log para o processamento de
   * arquivos.
   *
   * @author Gordo&#153;
   * @param  _logLevel nível de log para o processamento
   */
  public void setLogLevel(int _logLevel) {
    this.logLevel = _logLevel;
  }

  /**
   * Instancia a variável da classe referente ao ConnectionPool com a instancia
   * recebida por parâmetro. Faz-se isso para garantir que toda a aplicação
   * utilizará apenas um único connection pool.
   *
   * @author Gordo&#153;
   * @param  _connectionPool instancia da classe ConnectionPool a ser utilizada
   */
  public void setConnectionPool(ConnectionPool _connectionPool) {
    this.connectionPool = _connectionPool;
  }

  /**
   * Instancia a variável da classe responsável por gerar os logs. Faz-se isso
   * para garantir que toda a aplicação utilize a mesma instancia para geração
   * de log.
   *
   * @author Gordo&#153;
   * @param  _logMessages instancia da classe de log
   */
  public void setLogGenerator(LogMessages _logMessages) {
    this.logApplication = _logMessages;
  }

  /**
   * Método responsável por setar para a classe de processamento qual é o arquivo
   * que deve ser processado.
   *
   * @author Gordo&#153;
   * @param  _file instância da classe File que representa o arquivo a ser processado
   */
  public void setFile(File _file) {
    this.file2Process = _file;
  }

  /**
   * Seta a propriedade da classe que contém as informações sobre o tipo de
   * arquivo a ser processado.
   *
   * @author Gordo&#153;
   * @param  _validFile informações sobre o tipo de arquivo a ser procesado
   */
  public void setValidFile(ValidFile _validFile) {
    this.validFile = _validFile;
  }

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos processados
   * com sucesso serão movidos.
   *
   * @author Gordo&#153;
   * @param  _processedDir arquivo de destino dos arquivos processados com sucesso
   */
  public void setProcessedDir(String _processedDir) {
    this.processedDir = _processedDir;
  }

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos que apresentarem
   * algum problema durante o seu processamento.
   *
   * @author Gordo&#153;
   * @param  _processedDir arquivo de destino dos arquivos processados com erro(s)
   */
  public void setIrregularDir(String _irregularDir) {
    this.irregularDir = _irregularDir;
  }

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos rejeitados
   * serão copiados.
   *
   * @author Gordo&#153;
   * @param  _rejectedDir arquivo de destino dos arquivos rejeitados
   */
  public void setRejectedDir(String _rejectedDir) {
    this.rejectedDir = _rejectedDir;
    // verifica se o diretorio está formatado corretamente
    if (!(File.separator.equals(this.rejectedDir.substring(this.rejectedDir.length() - 1, this.rejectedDir.length()))))
      this.rejectedDir = this.rejectedDir + File.separator;
  }

  /**
   * Seta a propriedade que indica qual é o tempo máximo (em segundos) que uma
   * thread pode ficar ociosa. Extrapolado este tempo e respeitando outras regras,
   * a thread será eliminada.
   *
   * @author Gordo&#153;
   * @param  _idleTime tempo maximo (em segundos) para um thread ficar ociosa
   */
  public void setIdleTime(int _idleTime) {
    idleTime = _idleTime;
  }

  /**
   * Seta a propriedade que indica qual é o tempo (em milissegundos) para
   * o sleep de uma thread.
   *
   * @author Gordo&#153;
   * @param  _sleepTime tempo (em milissegundos) para o sleep de uma thread.
   */
  public void setSleepTime(int _sleepTime) {
    sleepTime = _sleepTime;
  }

  /**
   * Seta a propriedade que indica qual é a url do servlet responsável por gerar
   * um novo id.
   *
   * @author Gordo&#153;
   * @param  _url url do servlet responsável por gerar um novo id.
   */
  public void setNewIdUrl(String _url) {
    newIdUrl = _url;
  }

  /**
   * Seta a propriedade da classe que indica qual é o tipo de arquivo processado
   * pela classe.
   *
   * @author Gordo&#153;
   * @param  _pattern o tipo de arquivo que a classe processa
   */
  public void setFilePattern(String _pattern) {
    filePattern = _pattern;
  }

  /**
   * Retorna qual tipo de arquivo é processado pela classe.
   *
   * @author Gordo&#153;
   * @return o tipo de arquivo que a classe processa
   */
  public String getFilePattern() {
    return filePattern;
  }

  /**
   * Retorna o conteúdo do método Thread.isAlive().
   *
   * @author Gordo&#153;
   * @return método Thread.isAlive()
   */
  public boolean isThreadAlive() {
    return this.isAlive();
  }

  /**
   * Método que retorna o valor da propriedade da classe que indica se o processamento
   * está ativo ou não. Diretamente influenciada pelo método setPause().
   *
   * @author Gordo&#153;
   * @return true se o processamento estiver em andamento, false caso contrário
   * @see    #pauseThread(boolean _pause)
   */
  public boolean isActive() {
    return this.active;
  }

  /**
   * Método que retorna o conteúdo da variável que indica quando foi finalizado
   * o último processo pela instãncia. É utilizado para monitorar as threas que
   * estão criadas porém não são utilizadas.
   *
   * @author Gordo&#153;
   * @return Date que indica quando foi realizado o último processamento pela classe
   */
  public java.util.Date getLastAccess() {
    return this.lastAccess;
  }

  /**
   * Método que instancia a classe para fazer o log do processamento deste arquivo.
   *
   * @author Gordo&#153;
   */
  public void setLogProcess() {
    try {
      logProcess = new LogMessages (validFile.getFileProcessLog(),
                                    this.logLevel,
                                    false);
    }
    catch (Exception ex) {
      logApplication.log("[jFileReceiver.files.GeneralFile] Erro na tentativa de se instanciar a classe para o log de processamento de arquivos " + this.getFilePattern() + ". " + ex.toString() + ". O log deste processo estará no log da aplicação.", logApplication.logGenerator.ERROR);
      logProcess = logApplication;
    }
  }

  /**
   * Método que realiza as operações para finalizar um processamento de arquivo.
   *
   * @author Gordo&#153;
   * @param  _irregularFile true se for para um arquivo irregular, false para arquivos processados
   * @param  _unicFileError true se tiver ocorrido algum erro ao se gerar um único arquivo como resultado do processamento
   */
  public void endProcess(boolean _irregularFile, boolean _unicFileError) {
    try {
      // se o arquivo estiver irregular
      if (_irregularFile)
        // se houver arquivo único de processamento
        if (!("".equals(validFile.getOutputFile())))
          // se ocorreu nenhum problema na geração do mesmo
          if (_unicFileError) {
            file2Process = fileTools.moveFile(file2Process, validFile.getIrregularHomeOutBoxDir());
            file2Process = fileTools.unlockFile(file2Process, false, true, validFile.getOverwriteFiles());
          }
          else
            file2Process.delete();
        else {
          file2Process = fileTools.moveFile(file2Process, validFile.getIrregularHomeOutBoxDir());
          file2Process = fileTools.unlockFile(file2Process, false, true, validFile.getOverwriteFiles());
        }
      // se for um arquivo processado
      else
        // se houver arquivo único de processamento
        if (!("".equals(validFile.getOutputFile())))
          // se ocorreu nenhum problema na geração do mesmo
          if (_unicFileError) {
            file2Process = fileTools.moveFile(file2Process, validFile.getProcessedHomeOutBoxDir());
            file2Process = fileTools.unlockFile(file2Process, false, true, validFile.getOverwriteFiles());
            file2Process = fileTools.copyFile(file2Process,
                                              validFile.getIrregularHomeOutBoxDir() + file2Process.separator + file2Process.getName(),
                                              validFile.getOverwriteFiles());
          }
          else
            file2Process.delete();
        else {
          file2Process = fileTools.moveFile(file2Process, validFile.getProcessedHomeOutBoxDir());
          file2Process = fileTools.unlockFile(file2Process, false, true, validFile.getOverwriteFiles());
        }
      // logando status
      logProcess.log("[jFileReceiver.files.GeneralFile] Finalizado o processamento do arquivo " + file2Process.getPath() + ".", logProcess.logGenerator.STATUS);
    }
    catch (Exception fileException) {
      logApplication.log("[jFileReceiver.files.GeneralFile] Problemas ao se manipular o arquivo " + file2Process.getPath() + ". " + fileException.toString(), logApplication.logGenerator.ERROR);
      logProcess.log("[jFileReceiver.files.GeneralFile] Problemas ao se manipular o arquivo " + file2Process.getPath() + ". " + fileException.toString(), logProcess.logGenerator.ERROR);
    }
  }

  /**
   * Método responsável por colocar em um arquivo as linhas processadas pelo
   * jFileReceiver.
   *
   * @author Gordo&#153;
   * @param  _line linha a ser colocada no arquivo
   * @param  _processed true se for para "logar" a linha processada, false para logar a linha irregular
   */
  public void line2File (String _line, boolean _processed) throws Exception {
    String fileName = new String("");
    // monta o nome do arquivo para linhas processadas
    if (_processed) {
      fileName = validFile.getProcessedHomeOutBoxDir() + file2Process.separator + validFile.getOutputFile();
      // se deve gerar arquivo diário
      if (validFile.getOutputDaily())
        fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) +
                   DataTools.getSysdate("yyyyMMdd") +
                   fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }
    // monta o nome do arquivo para linhas irregulares
    else {
      fileName = validFile.getIrregularHomeOutBoxDir() + file2Process.separator + validFile.getOutputFile();
      // se deve gerar arquivo diário
      if (validFile.getOutputDaily())
        fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) +
                   DataTools.getSysdate("yyyyMMdd") +
                   fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }
    // instancia o BufferedWriter
    BufferedWriter outputLines = new BufferedWriter(new FileWriter(fileName, true));
    // escreve a linha no arquivo de saida
    outputLines.write(_line, 0, _line.length());
    outputLines.newLine();
    outputLines.flush();
    outputLines.close();
  }

  /**
   * Método que realiza várias tentativas de escrever no arquivo de saida uma
   * linha.
   *
   * @param _line linha a ser colocada no output
   * @param _processed true se for uma linha processada, false se for uma linha irregurlar
   * @param _indexLine número da linha
   */
  public void outputLine (String _line, boolean _processed, int _indexLine) {
    // variáveis para controle
    boolean hasException = false;
    String textException = new String("");
    try {
      // verifica se o tipo de arquivo tem saída única
      if (!("".equals(validFile.getOutputFile()))) {
        boolean sucess = false;
        int amountTry = 0;
        // tenta MAX_TRY vezes escrever a linha no arquivo de saida
        while ((! sucess) && (! stop)){
          try {
            this.line2File(_line, _processed);
            sucess = true;
          }
          catch (Exception ex) {
            // já tentou o número máximo de vezes
            if (amountTry == MAX_TRY) {
              this.errorProcess = true;
              throw ex;
            }
            // controle
            hasException = true;
            textException = ex.toString();
            if (! stop)
              // sleep para dar uma espera para que o recurso, caso esteja desabilitado, seja habilitado novamente
              Thread.sleep(this.sleepTime * 100);
          }
          amountTry ++;
        }
      }
      //
      if ((stop) && (hasException)) {
        this.errorProcess = true;
        throw new Exception (textException);
      }
    }
    catch (Exception outputException) {
      logProcess.log("[jFileReceiver.files.GeneralFile] Erro na tentativa de colocar a linha " + _indexLine + " do arquivo " + file2Process.getPath() + " no arquivo de saída." + outputException.toString(), logProcess.logGenerator.ERROR);
      logApplication.log("[jFileReceiver.files.GeneralFile] Erro na tentativa de colocar a linha " + _indexLine + " do arquivo " + file2Process.getPath() + " no arquivo de saída." + outputException.toString(), logApplication.logGenerator.ERROR);
      this.errorProcess = true;
    }
  }

  /**
   * Método responsável por obter a versão a linha se refere. Recebe como paramêtro
   * o length da linha.
   *
   * @author Gordo&#153;
   * @param  _lengthLine tamanho da linha que está sendo processada.
   * @return o índice da versão nas configurações
   */
  public int getVersion(int _lengthLine) throws Exception {
    int result = -1;
    FileVersion fileVersion;
    TxtVersion txtVersion;
    // percorre as versões procurando alguma versão que seja válida
    for (int indexVersion = 0; indexVersion < validFile.getFileVersionsSize(); indexVersion++) {
      // obtem as informações sobre uma versão
      fileVersion = validFile.getFileVersions(indexVersion);
      txtVersion = fileVersion.getTxtVersion();
      // verifica se existe uma versão txt para esta versão de arquivo
      if (txtVersion != null) {
        // se achar uma versão compatível com o tamanho da linha
        if (_lengthLine == txtVersion.getLineSize()) {
          // se já tiver achado uma versão para este tamanho de linha
          if (result > -1)
            throw new Exception ("Foram encontradas mais de uma versão para aquivos " + validFile.getFilePattern() + " com tamanho de linha igual a " + _lengthLine + ". Acerte as configurações do jFileReceiver.");
          else
            result = indexVersion;
        }
      }
    }
    return result;
  }

  /**
   * Método responsável por obter a versão a linha se refere. Recebe como paramêtro
   * o código da versão.
   *
   * @author Gordo&#153;
   * @param  _version código da versao.
   * @return o índice da versão nas configurações
   */
  public int getVersion(String _version) throws Exception {
    int result = -1;
    FileVersion fileVersion;
    XmlVersion xmlVersion;
    // percorre as versões procurando alguma versão que seja válida
    for (int indexVersion = 0; indexVersion < validFile.getFileVersionsSize(); indexVersion++) {
      // obtem as informações sobre uma versão
      fileVersion = validFile.getFileVersions(indexVersion);
      // se achar uma versão igual a _version
      if (_version.equals(fileVersion.getCodeVersion())) {
        // se já tiver achado uma versão para esta _version
        if (result > -1)
          throw new Exception ("Foram encontradas mais de uma versão para aquivos " + validFile.getFilePattern() + ". Acerte as configurações do jFileReceiver.");
        else
          result = indexVersion;
      }
    }
    return result;
  }

  /**
   * Executa um comando SQL AMOUNT_TRY vezes. Caso não consiga executar em nenhuma
   * das tentativas, da um throws na exceção.
   *
   * @author Gordo&#153;
   * @param  _connectionPool connectionPool a ser utilizado para executar o comando SQL
   * @param  _query comando SQL a ser executado
   */
  public void execute (String _query) throws Exception, SQLException {
    // variáveis para controle
    boolean hasException = false;
    String textException = new String("");
    //
    boolean sucess = false;
    // realiza as tentativas até obter sucesso ou ultrapassar o máximo de tentativas ou a Thread ser setada para parar
    for (int attempt = 1; (! sucess) && (attempt <= AMOUNT_TRY) && (! this.stop); attempt ++) {
      try {
        this.connectionPool.execute(_query);
        sucess = true;
      }
      catch (SQLException sqlException) {
        if (attempt == AMOUNT_TRY)
          throw sqlException;
        // controle
        hasException = true;
        textException = sqlException.toString();
        if (! this.stop)
          Thread.sleep(10000);
      }
    }
    //
    if ((stop) && (hasException))
      throw new Exception(textException);
  }

  /**
   * Executa um comando SQL AMOUNT_TRY vezes. Caso não consiga executar em nenhuma
   * das tentativas, da um throws na exceção. Difere-se do outro método execute()
   * por receber por parametro o handler a ser utilizado pelo connectionPool. Deve
   * ser utilizado para se realizar transações no banco de dados.
   *
   * @author Gordo&#153;
   * @param  _connectionPool connectionPool a ser utilizado para executar o comando SQL
   * @param  _query comando SQL a ser executado
   * @param  _handler handler a ser utilizado pelo connectionPool
   */
  public void execute (String _query, int _handler) throws Exception, SQLException {
    // variáveis para controle
    boolean hasException = false;
    String textException = new String("");
    //
    boolean sucess = false;
    // realiza as tentativas até obter sucesso ou ultrapassar o máximo de tentativas ou a Thread ser setada para parar
    for (int attempt = 1; (! sucess) && (attempt <= AMOUNT_TRY)  && (! this.stop); attempt ++) {
      try {
        this.connectionPool.execute(_query, _handler);
        sucess = true;
      }
      catch (SQLException sqlException) {
        if (attempt == AMOUNT_TRY)
          throw sqlException;
        // controle
        hasException = true;
        textException = sqlException.toString();
        if (! this.stop)
          Thread.sleep(10000);
      }
    }
    //
    if ((stop) && (hasException))
      throw new Exception (textException);
  }

  /**
   * Executa uma query (select) AMOUNT_TRY vezes. Caso não consiga executar em nenhuma
   * das tentativas, da um throws na exceção.
   *
   * @author Gordo&#153;
   * @param  _connectionPool connectionPool a ser utilizado para executar o comando SQL
   * @param  _query comando SQL a ser executado
   * @return o result set contendo as informações retornadas pelo select realizado
   */
  public ResultSet executeQuery (String _query) throws Exception, SQLException {
    // variáveis para controle
    boolean hasException = false;
    String textException = new String("");
    //
    ResultSet resultSet = null;
    boolean sucess = false;
    // realiza as tentativas até obter sucesso ou ultrapassar o máximo de tentativas ou a Thread ser setada para parar
    for (int attempt = 1; (! sucess) && (attempt <= AMOUNT_TRY)  && (! this.stop); attempt ++) {
      try {
        resultSet = this.connectionPool.executeQuery(_query);
        sucess = true;
      }
      catch (SQLException sqlException) {
        if (attempt == AMOUNT_TRY)
          throw sqlException;
        // controle
        hasException = true;
        textException = sqlException.toString();
        if (! this.stop)
          Thread.sleep(10000);
      }
    }
    //
    if ((stop) && (hasException))
      throw new Exception(textException);
    //
    return resultSet;
  }

}