package jfilereceiver.conf;

import util.xmlparser.*;
import java.util.LinkedList;
import java.io.File;

/**
 * Title:        Configuration
 * Description:  Classe respons�vel por obter as configura��es do arquivo conf
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog - Empresa Brasileira de Log�stica
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class Configuration {

  // constantes sobre o arquivo de configura��o
  public static final String CONF_FILE_NAME = "jFileReceiver-conf.xml";

  // constantes sobre os elementos do XML de configura��o
  static final String XPATH_INBOX      = "/jFileReceiver-conf/jfrc-inboxes/jfrc-inbox";
  static final String XPATH_APPLOG     = "/jFileReceiver-conf/jfcr-applicationLog";
  static final String XPATH_CONNDRIVER = "/jFileReceiver-conf/jfrc-databaseConnection/dbconn-driver";
  static final String XPATH_CONNURL    = "/jFileReceiver-conf/jfrc-databaseConnection/dbconn-url";
  static final String XPATH_CONNUSER   = "/jFileReceiver-conf/jfrc-databaseConnection/dbconn-username";
  static final String XPATH_CONNPASS   = "/jFileReceiver-conf/jfrc-databaseConnection/dbconn-password";
  static final String XPATH_VALIDFILES = "/jFileReceiver-conf/jfrc-validFiles/jfrc-validFile";
  static final String XPATH_DEBUGLEVEL = "/jFileReceiver-conf/jfrc-debugLevel";
  static final String XPATH_IDLETIME   = "/jFileReceiver-conf/jfrc-idleTime";
  static final String XPATH_SLEEPTIME  = "/jFileReceiver-conf/jfrc-sleepTime";
  static final String XPATH_REJECDIR   = "/jFileReceiver-conf/jfrc-rejectedOutBox";
  static final String XPATH_NEWIDURL   = "/jFileReceiver-conf/jfrc-newIdUrl";
  static final String XPATH_OUTBOXDIR  = "jfrob-outBox";
  static final String XPATH_DAILYDIR   = "jfrob-dailyDirectory";

  // constantes sobre os elementos opcionais do XML de configura��o
  static final int DEFAULT_IDLETIME  = 120;
  static final int DEFAULT_SLEEPTIME = 1000;

  // vari�veis
  public String confDir = new String("");
  XMLDocument confFile;
  LinkedList validFiles = new LinkedList();
  LinkedList inboxes = new LinkedList();
  String applicationLog = new String("");
  String dbConnDriver = new String("");
  String dbConnUrl = new String("");
  String dbConnUsername = new String("");
  String dbConnPassword = new String("");
  String newIdUrl = new String("");
  int debugLevel;
  int idleTime;
  int sleepTime;
  OutBox rejectedHome = new OutBox();

  /**
   * Construtor da classe Configuration. Abre o arquivo XML de configura��es
   * para acesso as suas informa��es
   *
   * @author Gordo&#153;
   * @param  _homeConf diretorio que cont�m o arquivo de configura��es
   * @throws LockException se o documento especificado j� estava travado para escrita ou se houve um erro de I/O ao criar o arquivo lock
   * @throws ParseException se houver um erro na leitura do documento
   */
  public Configuration(String _homeConf) {
    try {
      StringBuffer openError = new StringBuffer();
      String operationSystem = System.getProperty("os.name","linux");
      // verifica se _homeConf esta formatada corretamente
      if (!(File.separator.equals(_homeConf.substring(_homeConf.length() - 1, _homeConf.length()))))
        confDir = _homeConf + File.separator;
      // abre o arquivo de configura��es
      confFile = new XMLDocument (confDir + CONF_FILE_NAME, true, openError);
      // verificando se ocorreu algum erro durante a opera��o
      if (!("".equals(openError.toString())))
        throw new Exception ("[Configuration]: Erro ao abrir o arquivo " + confDir + CONF_FILE_NAME + ". " + openError.toString());
    }
    catch (Exception ex) {
      System.out.println(ex.toString());
      System.exit(0);
    }
  }

  /**
   * Retorna o conte�do de um elemento, em formato String, representado pelo xPath
   * recebido por par�metro.
   *
   * @author Gordo&#153;
   * @param  _xpath xPath que identifica o elemento que se deseja obter o conte�do
   * @return o conte�do de _xpath
   * @throws NullPointerException se o xPath for inv�lido
   */
  public String getStringValue(String _xpath) throws Exception {
    return confFile.search(_xpath).getElement(0).getValue();
  }

  /**
   * Retorna o conte�do de um elemento, em formato Integer, representado pelo xPath
   * recebido por par�metro.
   *
   * @author Gordo&#153;
   * @param  _xpath xPath que identifica o elemento que se deseja obter o conte�do
   * @return o conte�do de _xpath
   * @throws NullPointerException se o xPath for inv�lido
   */
  public Integer getIntegerValue(String _xpath) throws Exception {
    return confFile.search(_xpath).getElement(0).getValueInt();
  }

  /**
   * Obt�m do arquivo de configura��es os diret�rios que devem ser monitorados.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca dos inboxes for inv�lido
   */
  public void setInboxes() throws Exception {
    inboxes.clear();
    ElementList inboxesList = confFile.search(XPATH_INBOX);
    // percorre os valores devolvidos pelo search
    for (int index = 0; index < inboxesList.getSize(); index ++)
      inboxes.add(inboxesList.getElement(index).getValue());
  }

  /**
   * Obt�m do arquivo de configura��es o arquivo de log da aplica��o
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do arquivo de log da aplica��o for inv�lido
   */
  public void setApplicationLog() throws Exception {
    applicationLog = confFile.search(XPATH_APPLOG).getElement(0).getValue();
  }

  /**
   * Obt�m do arquivo de configura��es o driver para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do driver de conex�o for inv�lido
   */
  public void setDbConnectionDriver() throws Exception {
    dbConnDriver = confFile.search(XPATH_CONNDRIVER).getElement(0).getValue();
  }

  /**
   * Obt�m do arquivo de configura��es a url para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca da url de conex�o for inv�lido
   */
  public void setDbConnectionUrl() throws Exception {
    dbConnUrl = confFile.search(XPATH_CONNURL).getElement(0).getValue();
  }

  /**
   * Obt�m do arquivo de configura��es o username para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do username de conex�o for inv�lido
   */
  public void setDbConnectionUsername() throws Exception {
    dbConnUsername = confFile.search(XPATH_CONNUSER).getElement(0).getValue();
  }

  /**
   * Obt�m do arquivo de configura��es a senha para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca da senha de conex�o for inv�lido
   */
  public void setDbConnectionPassword() throws Exception {
    dbConnPassword = confFile.search(XPATH_CONNPASS).getElement(0).getValue();
  }

  /**
   * Obt�m do arquivo de configura��es o n�vel de debug da aplica��o
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do n�vel de debug for inv�lido
   */
  public void setDebugLevel() throws Exception {
    debugLevel = confFile.search(XPATH_DEBUGLEVEL).getElement(0).getValueInt().intValue();
  }

  /**
   * Obt�m do arquivo de configura��es o tempo (em milissegundos) padr�o para os sleeps da
   * aplica��o.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do sleepTime for inv�lido
   */
  public void setSleepTime() throws Exception {
    // atribui o valor default ao atributo. Caso n�o ache o elemento no XML este valor sera o v�lido
    sleepTime = DEFAULT_SLEEPTIME;
    ElementList elementList = confFile.search(XPATH_SLEEPTIME);
    for (int indexList = 0; indexList < elementList.getSize(); indexList ++)
      sleepTime = elementList.getElement(indexList).getValueInt().intValue();
  }

  /**
   * Obt�m do arquivo de configura��es o tempo (em milissegundos) padr�o para que
   * uma thread possa ficar inativa sem ser finalizada.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do idleTime for inv�lido
   */
  public void setIdleTime() throws Exception {
    // atribui o valor default ao atributo. Caso n�o ache o elemento no XML este valor sera o v�lido
    idleTime = DEFAULT_IDLETIME;
    ElementList elementList = confFile.search(XPATH_IDLETIME);
    for (int indexList = 0; indexList < elementList.getSize(); indexList ++)
      idleTime = elementList.getElement(indexList).getValueInt().intValue();
  }

  /**
   * Obt�m do arquivo de configura��es as informa��es do diret�rio no qual os
   * arquivos que n�o forem processados pelo jFileReceiver ser�o colocados.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do rejectedHome for inv�lido
   */
  public void setRejectedHome() throws Exception {
    rejectedHome.setOutBoxDir(confFile.search(XPATH_REJECDIR + "/" + XPATH_OUTBOXDIR).getElement(0).getValue());
    rejectedHome.setDailyDir(confFile.search(XPATH_REJECDIR + "/" + XPATH_DAILYDIR).getElement(0).getValueBoolean().booleanValue());
  }

  /**
   * Obt�m do arquivo de configura��es a URL do servlet respons�vel por obter um
   * novo id
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do rejectedHome for inv�lido
   */
  public void setNewIdUrl() throws Exception {
    newIdUrl = confFile.search(XPATH_NEWIDURL).getElement(0).getValue();
  }

  /**
   * Retorna um inbox da lista de inboxes a ser monitoradas.
   *
   * @author Gordo&#153;
   * @return a inbox equivalente a posi��o _index na lista de inboxes
   * @param  _index posi��o da inbox desejada
   * @throws NullPointerException se o xPath para a busca dos inboxes for inv�lido
   */
  public String getInbox(int _index) throws Exception {
    return (String) inboxes.get(_index);
  }

  /**
   * Obt�m do arquivo de configura��es o arquivo de log da aplica��o
   *
   * @author Gordo&#153;
   * @return path completo do arquivo de log da aplica��o
   * @throws NullPointerException se o xPath para a busca do arquivo de log da aplica��o for inv�lido
   */
  public String getApplicationLog() throws Exception {
    return applicationLog;
  }

  /**
   * Obt�m do arquivo de configura��es o driver para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @return driver para conex�o com o banco de dados
   * @throws NullPointerException se o xPath para a busca do driver de conex�o for inv�lido
   */
  public String getDbConnectionDriver() throws Exception {
    return dbConnDriver;
  }

  /**
   * Obt�m do arquivo de configura��es a url para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @return url para conex�o com o banco de dados
   * @throws NullPointerException se o xPath para a busca da url de conex�o for inv�lido
   */
  public String getDbConnectionUrl() throws Exception {
    return dbConnUrl;
  }

  /**
   * Obt�m do arquivo de configura��es o username para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @return username para conex�o com o banco de dados
   * @throws NullPointerException se o xPath para a busca do username de conex�o for inv�lido
   */
  public String getDbConnectionUsername() throws Exception {
    return dbConnUsername;
  }

  /**
   * Obt�m do arquivo de configura��es a senha para conex�o com o banco de dados
   *
   * @author Gordo&#153;
   * @return senha para conex�o com o banco de dados
   * @throws NullPointerException se o xPath para a busca da senha de conex�o for inv�lido
   */
  public String getDbConnectionPassword() throws Exception {
    return dbConnPassword;
  }

  /**
   * @return uma inst�ncia da classe ValidFile contendo as informa��es sobre um tipo de arquivo v�lido
   * @author Gordo&#153;
   */
  public ValidFile getValidFileProperties (int _index) throws Exception {
    return (ValidFile) validFiles.get(_index);
  }

  /**
   * Realiza o refresh do arquivo de configura��o.
   *
   * @author Gordo&#153;
   * @throws LockException se o documento especificado j� estava travado para escrita ou se houve um erro de I/O ao criar o arquivo lock
   * @throws ParseException se houver um erro na leitura do documento
   */
  public void refresh() throws Exception {
    confFile.close();
    confFile = new XMLDocument (confDir + CONF_FILE_NAME, true);
    this.setProperties();
  }

  /**
   * Realiza o preenchimento de todas as propriedades da classe.
   *
   * @author Gordo&#153;
   */
  public void setProperties () throws Exception {
    this.setInboxes();
    this.setApplicationLog();
    this.setDebugLevel();
    this.setIdleTime();
    this.setSleepTime();
    this.setRejectedHome();
    this.setNewIdUrl();
    // obtendo as configura��es para conex�o com o banco de dados
    this.setDbConnectionDriver();
    this.setDbConnectionUrl();
    this.setDbConnectionUsername();
    this.setDbConnectionPassword();
    // obtendo os tipos de arquivos v�lidos e as informa��es sobre o mesmo
    validFiles.clear();
    ElementList valids = confFile.search(XPATH_VALIDFILES);
    ValidFile validFile;
    for (int index = 0; index < valids.getSize(); index ++)
    {
      validFile = new ValidFile();
      validFile.setFileClassName(valids.getElement(index));
      validFile.setFileDescription(valids.getElement(index));
      validFile.setFilePattern(valids.getElement(index));
      validFile.setFileProcessLog(valids.getElement(index));
      validFile.setFileVersions(valids.getElement(index));
      validFile.setMinThreads(valids.getElement(index));
      validFile.setMaxThreads(valids.getElement(index));
      validFile.setIrregularHome(valids.getElement(index));
      validFile.setProcessedHome(valids.getElement(index));
      validFile.setOverwriteFiles(valids.getElement(index));
      validFile.setUnicOutputFile(valids.getElement(index));
      validFile.setExtractTo(valids.getElement(index));
      validFiles.add(validFile);
    }
  }

  /**
   * @author Gordo&#153;
   * @return o tamanho da lista de inboxes
   */
  public int getInboxesSize () {
    return inboxes.size();
  }

  /**
   * @author Gordo&#153;
   * @return o tamanho da lista de tipos de arquivos v�lidos
   */
  public int getValidFilesSize () {
    return validFiles.size();
  }

  /**
   * @author Gordo&#153;
   * @return o n�vel de debug da aplica��o
   */
  public int getDebugLevel () {
    return debugLevel;
  }

  /**
   * @author Gordo&#153;
   * @return o sleepTime da aplica��o
   */
  public int getSleepTime () {
    return sleepTime;
  }

  /**
   * @author Gordo&#153;
   * @return o n�vel de debug da aplica��o
   */
  public int getIdleTime () {
    return idleTime;
  }

  /**
   * @author Gordo&#153;
   * @return o diret�rio no qual dever�o ser colocado os arquivos que n�o forem processados pelo jFileReceiver
   */
  public String getRejectedHomeOutBoxDir () throws Exception {
    return rejectedHome.getOutBoxDir();
  }

  /**
   * @author Gordo&#153;
   * @return o diret�rio principal de arquivos rejeitados
   */
  public String getRejectedHomeOutBoxDirNoDate () {
    return rejectedHome.getOutBoxDirNoDate();
  }

  /**
   * @author Gordo&#153;
   * @return true caso deva se criar subdiretorios no rejectedHome
   */
  public boolean getRejectedHomeOutBoxDaily () {
    return rejectedHome.getDailyDir();
  }

  /**
   * @author Gordo&#153;
   * @return a url do servlet respons�vel por gerar um novo id
   */
  public String getNewIdUrl () {
    return newIdUrl;
  }

}