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
 * Description:  Classe que implementa a interface FileInterface e herda os m�todos da
 *               classe java.lang.Thread. Representa um arquivo qualquer que � processado
 *               pelo jFileReceiver.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class GeneralFile extends java.lang.Thread implements jfilereceiver.files.FileInterface {

  // constantes
  public static final int MAX_TRY = 10;

  // n�mero m�ximo de tentativas para realizar uma opera��o no banco de dados
  static final int AMOUNT_TRY = 3;

  // vari�veis
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
  // vari�vel que indicar� se a thread est� ativa ou n�o. Seu valor � diretamente influenciado
  // pelo m�todo setPauseThread()
  public boolean active = false;
  // vari�vel que indicar� se a thread deve parar ou n�o. Seu valor � diretamente influenciado
  // pelo m�todo setStopThread()
  public boolean stop = false;
  //
  public boolean errorProcess = false;
  public boolean irregular = false;

  /**
   * M�todo respons�vel por iniciar uma thread. Basicamente � para fazer um
   * this.start na classe para que o m�todo run() possa ser "acionado".
   *
   * @author Gordo&#153;
   */
  public void startThread() {
    this.start();
  }

  /**
   * M�todo respons�vel por finalizar uma thread. Seta a propriedade que para o
   * loop principal do m�todo run().
   *
   * @author Gordo&#153;
   */
  public void stopThread() {
    this.stop = true;
  }

  /**
   * M�todo respons�vel por pausar ou despausar uma thread. Seta a propriedade
   * que indica se a thread est� ativa ou n�o de acordo com o valor de _pause.
   *
   * @author Gordo&#153;
   * @param  _pause true caso deseje pausar a thread, false caso contr�rio
   */
  public void pauseThread(boolean _pause) {
    this.active = ! _pause;
  }

  /**
   * Seta a propriedade que indica qual � o n�vel de log para o processamento de
   * arquivos.
   *
   * @author Gordo&#153;
   * @param  _logLevel n�vel de log para o processamento
   */
  public void setLogLevel(int _logLevel) {
    this.logLevel = _logLevel;
  }

  /**
   * Instancia a vari�vel da classe referente ao ConnectionPool com a instancia
   * recebida por par�metro. Faz-se isso para garantir que toda a aplica��o
   * utilizar� apenas um �nico connection pool.
   *
   * @author Gordo&#153;
   * @param  _connectionPool instancia da classe ConnectionPool a ser utilizada
   */
  public void setConnectionPool(ConnectionPool _connectionPool) {
    this.connectionPool = _connectionPool;
  }

  /**
   * Instancia a vari�vel da classe respons�vel por gerar os logs. Faz-se isso
   * para garantir que toda a aplica��o utilize a mesma instancia para gera��o
   * de log.
   *
   * @author Gordo&#153;
   * @param  _logMessages instancia da classe de log
   */
  public void setLogGenerator(LogMessages _logMessages) {
    this.logApplication = _logMessages;
  }

  /**
   * M�todo respons�vel por setar para a classe de processamento qual � o arquivo
   * que deve ser processado.
   *
   * @author Gordo&#153;
   * @param  _file inst�ncia da classe File que representa o arquivo a ser processado
   */
  public void setFile(File _file) {
    this.file2Process = _file;
  }

  /**
   * Seta a propriedade da classe que cont�m as informa��es sobre o tipo de
   * arquivo a ser processado.
   *
   * @author Gordo&#153;
   * @param  _validFile informa��es sobre o tipo de arquivo a ser procesado
   */
  public void setValidFile(ValidFile _validFile) {
    this.validFile = _validFile;
  }

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos processados
   * com sucesso ser�o movidos.
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
   * ser�o copiados.
   *
   * @author Gordo&#153;
   * @param  _rejectedDir arquivo de destino dos arquivos rejeitados
   */
  public void setRejectedDir(String _rejectedDir) {
    this.rejectedDir = _rejectedDir;
    // verifica se o diretorio est� formatado corretamente
    if (!(File.separator.equals(this.rejectedDir.substring(this.rejectedDir.length() - 1, this.rejectedDir.length()))))
      this.rejectedDir = this.rejectedDir + File.separator;
  }

  /**
   * Seta a propriedade que indica qual � o tempo m�ximo (em segundos) que uma
   * thread pode ficar ociosa. Extrapolado este tempo e respeitando outras regras,
   * a thread ser� eliminada.
   *
   * @author Gordo&#153;
   * @param  _idleTime tempo maximo (em segundos) para um thread ficar ociosa
   */
  public void setIdleTime(int _idleTime) {
    idleTime = _idleTime;
  }

  /**
   * Seta a propriedade que indica qual � o tempo (em milissegundos) para
   * o sleep de uma thread.
   *
   * @author Gordo&#153;
   * @param  _sleepTime tempo (em milissegundos) para o sleep de uma thread.
   */
  public void setSleepTime(int _sleepTime) {
    sleepTime = _sleepTime;
  }

  /**
   * Seta a propriedade que indica qual � a url do servlet respons�vel por gerar
   * um novo id.
   *
   * @author Gordo&#153;
   * @param  _url url do servlet respons�vel por gerar um novo id.
   */
  public void setNewIdUrl(String _url) {
    newIdUrl = _url;
  }

  /**
   * Seta a propriedade da classe que indica qual � o tipo de arquivo processado
   * pela classe.
   *
   * @author Gordo&#153;
   * @param  _pattern o tipo de arquivo que a classe processa
   */
  public void setFilePattern(String _pattern) {
    filePattern = _pattern;
  }

  /**
   * Retorna qual tipo de arquivo � processado pela classe.
   *
   * @author Gordo&#153;
   * @return o tipo de arquivo que a classe processa
   */
  public String getFilePattern() {
    return filePattern;
  }

  /**
   * Retorna o conte�do do m�todo Thread.isAlive().
   *
   * @author Gordo&#153;
   * @return m�todo Thread.isAlive()
   */
  public boolean isThreadAlive() {
    return this.isAlive();
  }

  /**
   * M�todo que retorna o valor da propriedade da classe que indica se o processamento
   * est� ativo ou n�o. Diretamente influenciada pelo m�todo setPause().
   *
   * @author Gordo&#153;
   * @return true se o processamento estiver em andamento, false caso contr�rio
   * @see    #pauseThread(boolean _pause)
   */
  public boolean isActive() {
    return this.active;
  }

  /**
   * M�todo que retorna o conte�do da vari�vel que indica quando foi finalizado
   * o �ltimo processo pela inst�ncia. � utilizado para monitorar as threas que
   * est�o criadas por�m n�o s�o utilizadas.
   *
   * @author Gordo&#153;
   * @return Date que indica quando foi realizado o �ltimo processamento pela classe
   */
  public java.util.Date getLastAccess() {
    return this.lastAccess;
  }

  /**
   * M�todo que instancia a classe para fazer o log do processamento deste arquivo.
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
      logApplication.log("[jFileReceiver.files.GeneralFile] Erro na tentativa de se instanciar a classe para o log de processamento de arquivos " + this.getFilePattern() + ". " + ex.toString() + ". O log deste processo estar� no log da aplica��o.", logApplication.logGenerator.ERROR);
      logProcess = logApplication;
    }
  }

  /**
   * M�todo que realiza as opera��es para finalizar um processamento de arquivo.
   *
   * @author Gordo&#153;
   * @param  _irregularFile true se for para um arquivo irregular, false para arquivos processados
   * @param  _unicFileError true se tiver ocorrido algum erro ao se gerar um �nico arquivo como resultado do processamento
   */
  public void endProcess(boolean _irregularFile, boolean _unicFileError) {
    try {
      // se o arquivo estiver irregular
      if (_irregularFile)
        // se houver arquivo �nico de processamento
        if (!("".equals(validFile.getOutputFile())))
          // se ocorreu nenhum problema na gera��o do mesmo
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
        // se houver arquivo �nico de processamento
        if (!("".equals(validFile.getOutputFile())))
          // se ocorreu nenhum problema na gera��o do mesmo
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
   * M�todo respons�vel por colocar em um arquivo as linhas processadas pelo
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
      // se deve gerar arquivo di�rio
      if (validFile.getOutputDaily())
        fileName = fileName.substring(0, fileName.lastIndexOf(".") + 1) +
                   DataTools.getSysdate("yyyyMMdd") +
                   fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }
    // monta o nome do arquivo para linhas irregulares
    else {
      fileName = validFile.getIrregularHomeOutBoxDir() + file2Process.separator + validFile.getOutputFile();
      // se deve gerar arquivo di�rio
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
   * M�todo que realiza v�rias tentativas de escrever no arquivo de saida uma
   * linha.
   *
   * @param _line linha a ser colocada no output
   * @param _processed true se for uma linha processada, false se for uma linha irregurlar
   * @param _indexLine n�mero da linha
   */
  public void outputLine (String _line, boolean _processed, int _indexLine) {
    // vari�veis para controle
    boolean hasException = false;
    String textException = new String("");
    try {
      // verifica se o tipo de arquivo tem sa�da �nica
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
            // j� tentou o n�mero m�ximo de vezes
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
      logProcess.log("[jFileReceiver.files.GeneralFile] Erro na tentativa de colocar a linha " + _indexLine + " do arquivo " + file2Process.getPath() + " no arquivo de sa�da." + outputException.toString(), logProcess.logGenerator.ERROR);
      logApplication.log("[jFileReceiver.files.GeneralFile] Erro na tentativa de colocar a linha " + _indexLine + " do arquivo " + file2Process.getPath() + " no arquivo de sa�da." + outputException.toString(), logApplication.logGenerator.ERROR);
      this.errorProcess = true;
    }
  }

  /**
   * M�todo respons�vel por obter a vers�o a linha se refere. Recebe como param�tro
   * o length da linha.
   *
   * @author Gordo&#153;
   * @param  _lengthLine tamanho da linha que est� sendo processada.
   * @return o �ndice da vers�o nas configura��es
   */
  public int getVersion(int _lengthLine) throws Exception {
    int result = -1;
    FileVersion fileVersion;
    TxtVersion txtVersion;
    // percorre as vers�es procurando alguma vers�o que seja v�lida
    for (int indexVersion = 0; indexVersion < validFile.getFileVersionsSize(); indexVersion++) {
      // obtem as informa��es sobre uma vers�o
      fileVersion = validFile.getFileVersions(indexVersion);
      txtVersion = fileVersion.getTxtVersion();
      // verifica se existe uma vers�o txt para esta vers�o de arquivo
      if (txtVersion != null) {
        // se achar uma vers�o compat�vel com o tamanho da linha
        if (_lengthLine == txtVersion.getLineSize()) {
          // se j� tiver achado uma vers�o para este tamanho de linha
          if (result > -1)
            throw new Exception ("Foram encontradas mais de uma vers�o para aquivos " + validFile.getFilePattern() + " com tamanho de linha igual a " + _lengthLine + ". Acerte as configura��es do jFileReceiver.");
          else
            result = indexVersion;
        }
      }
    }
    return result;
  }

  /**
   * M�todo respons�vel por obter a vers�o a linha se refere. Recebe como param�tro
   * o c�digo da vers�o.
   *
   * @author Gordo&#153;
   * @param  _version c�digo da versao.
   * @return o �ndice da vers�o nas configura��es
   */
  public int getVersion(String _version) throws Exception {
    int result = -1;
    FileVersion fileVersion;
    XmlVersion xmlVersion;
    // percorre as vers�es procurando alguma vers�o que seja v�lida
    for (int indexVersion = 0; indexVersion < validFile.getFileVersionsSize(); indexVersion++) {
      // obtem as informa��es sobre uma vers�o
      fileVersion = validFile.getFileVersions(indexVersion);
      // se achar uma vers�o igual a _version
      if (_version.equals(fileVersion.getCodeVersion())) {
        // se j� tiver achado uma vers�o para esta _version
        if (result > -1)
          throw new Exception ("Foram encontradas mais de uma vers�o para aquivos " + validFile.getFilePattern() + ". Acerte as configura��es do jFileReceiver.");
        else
          result = indexVersion;
      }
    }
    return result;
  }

  /**
   * Executa um comando SQL AMOUNT_TRY vezes. Caso n�o consiga executar em nenhuma
   * das tentativas, da um throws na exce��o.
   *
   * @author Gordo&#153;
   * @param  _connectionPool connectionPool a ser utilizado para executar o comando SQL
   * @param  _query comando SQL a ser executado
   */
  public void execute (String _query) throws Exception, SQLException {
    // vari�veis para controle
    boolean hasException = false;
    String textException = new String("");
    //
    boolean sucess = false;
    // realiza as tentativas at� obter sucesso ou ultrapassar o m�ximo de tentativas ou a Thread ser setada para parar
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
   * Executa um comando SQL AMOUNT_TRY vezes. Caso n�o consiga executar em nenhuma
   * das tentativas, da um throws na exce��o. Difere-se do outro m�todo execute()
   * por receber por parametro o handler a ser utilizado pelo connectionPool. Deve
   * ser utilizado para se realizar transa��es no banco de dados.
   *
   * @author Gordo&#153;
   * @param  _connectionPool connectionPool a ser utilizado para executar o comando SQL
   * @param  _query comando SQL a ser executado
   * @param  _handler handler a ser utilizado pelo connectionPool
   */
  public void execute (String _query, int _handler) throws Exception, SQLException {
    // vari�veis para controle
    boolean hasException = false;
    String textException = new String("");
    //
    boolean sucess = false;
    // realiza as tentativas at� obter sucesso ou ultrapassar o m�ximo de tentativas ou a Thread ser setada para parar
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
   * Executa uma query (select) AMOUNT_TRY vezes. Caso n�o consiga executar em nenhuma
   * das tentativas, da um throws na exce��o.
   *
   * @author Gordo&#153;
   * @param  _connectionPool connectionPool a ser utilizado para executar o comando SQL
   * @param  _query comando SQL a ser executado
   * @return o result set contendo as informa��es retornadas pelo select realizado
   */
  public ResultSet executeQuery (String _query) throws Exception, SQLException {
    // vari�veis para controle
    boolean hasException = false;
    String textException = new String("");
    //
    ResultSet resultSet = null;
    boolean sucess = false;
    // realiza as tentativas at� obter sucesso ou ultrapassar o m�ximo de tentativas ou a Thread ser setada para parar
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