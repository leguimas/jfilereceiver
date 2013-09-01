package jfilereceiver.conf;

import util.xmlparser.*;
import java.util.LinkedList;
import java.io.File;

/**
 * Title:        Configuration
 * Description:  Classe responsável por obter as configurações do arquivo conf
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog - Empresa Brasileira de Logística
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class Configuration {

  // constantes sobre o arquivo de configuração
  public static final String CONF_FILE_NAME = "jFileReceiver-conf.xml";

  // constantes sobre os elementos do XML de configuração
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

  // constantes sobre os elementos opcionais do XML de configuração
  static final int DEFAULT_IDLETIME  = 120;
  static final int DEFAULT_SLEEPTIME = 1000;

  // variáveis
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
   * Construtor da classe Configuration. Abre o arquivo XML de configurações
   * para acesso as suas informações
   *
   * @author Gordo&#153;
   * @param  _homeConf diretorio que contém o arquivo de configurações
   * @throws LockException se o documento especificado já estava travado para escrita ou se houve um erro de I/O ao criar o arquivo lock
   * @throws ParseException se houver um erro na leitura do documento
   */
  public Configuration(String _homeConf) {
    try {
      StringBuffer openError = new StringBuffer();
      String operationSystem = System.getProperty("os.name","linux");
      // verifica se _homeConf esta formatada corretamente
      if (!(File.separator.equals(_homeConf.substring(_homeConf.length() - 1, _homeConf.length()))))
        confDir = _homeConf + File.separator;
      // abre o arquivo de configurações
      confFile = new XMLDocument (confDir + CONF_FILE_NAME, true, openError);
      // verificando se ocorreu algum erro durante a operação
      if (!("".equals(openError.toString())))
        throw new Exception ("[Configuration]: Erro ao abrir o arquivo " + confDir + CONF_FILE_NAME + ". " + openError.toString());
    }
    catch (Exception ex) {
      System.out.println(ex.toString());
      System.exit(0);
    }
  }

  /**
   * Retorna o conteúdo de um elemento, em formato String, representado pelo xPath
   * recebido por parâmetro.
   *
   * @author Gordo&#153;
   * @param  _xpath xPath que identifica o elemento que se deseja obter o conteúdo
   * @return o conteúdo de _xpath
   * @throws NullPointerException se o xPath for inválido
   */
  public String getStringValue(String _xpath) throws Exception {
    return confFile.search(_xpath).getElement(0).getValue();
  }

  /**
   * Retorna o conteúdo de um elemento, em formato Integer, representado pelo xPath
   * recebido por parâmetro.
   *
   * @author Gordo&#153;
   * @param  _xpath xPath que identifica o elemento que se deseja obter o conteúdo
   * @return o conteúdo de _xpath
   * @throws NullPointerException se o xPath for inválido
   */
  public Integer getIntegerValue(String _xpath) throws Exception {
    return confFile.search(_xpath).getElement(0).getValueInt();
  }

  /**
   * Obtém do arquivo de configurações os diretórios que devem ser monitorados.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca dos inboxes for inválido
   */
  public void setInboxes() throws Exception {
    inboxes.clear();
    ElementList inboxesList = confFile.search(XPATH_INBOX);
    // percorre os valores devolvidos pelo search
    for (int index = 0; index < inboxesList.getSize(); index ++)
      inboxes.add(inboxesList.getElement(index).getValue());
  }

  /**
   * Obtém do arquivo de configurações o arquivo de log da aplicação
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do arquivo de log da aplicação for inválido
   */
  public void setApplicationLog() throws Exception {
    applicationLog = confFile.search(XPATH_APPLOG).getElement(0).getValue();
  }

  /**
   * Obtém do arquivo de configurações o driver para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do driver de conexão for inválido
   */
  public void setDbConnectionDriver() throws Exception {
    dbConnDriver = confFile.search(XPATH_CONNDRIVER).getElement(0).getValue();
  }

  /**
   * Obtém do arquivo de configurações a url para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca da url de conexão for inválido
   */
  public void setDbConnectionUrl() throws Exception {
    dbConnUrl = confFile.search(XPATH_CONNURL).getElement(0).getValue();
  }

  /**
   * Obtém do arquivo de configurações o username para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do username de conexão for inválido
   */
  public void setDbConnectionUsername() throws Exception {
    dbConnUsername = confFile.search(XPATH_CONNUSER).getElement(0).getValue();
  }

  /**
   * Obtém do arquivo de configurações a senha para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca da senha de conexão for inválido
   */
  public void setDbConnectionPassword() throws Exception {
    dbConnPassword = confFile.search(XPATH_CONNPASS).getElement(0).getValue();
  }

  /**
   * Obtém do arquivo de configurações o nível de debug da aplicação
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do nível de debug for inválido
   */
  public void setDebugLevel() throws Exception {
    debugLevel = confFile.search(XPATH_DEBUGLEVEL).getElement(0).getValueInt().intValue();
  }

  /**
   * Obtém do arquivo de configurações o tempo (em milissegundos) padrão para os sleeps da
   * aplicação.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do sleepTime for inválido
   */
  public void setSleepTime() throws Exception {
    // atribui o valor default ao atributo. Caso não ache o elemento no XML este valor sera o válido
    sleepTime = DEFAULT_SLEEPTIME;
    ElementList elementList = confFile.search(XPATH_SLEEPTIME);
    for (int indexList = 0; indexList < elementList.getSize(); indexList ++)
      sleepTime = elementList.getElement(indexList).getValueInt().intValue();
  }

  /**
   * Obtém do arquivo de configurações o tempo (em milissegundos) padrão para que
   * uma thread possa ficar inativa sem ser finalizada.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do idleTime for inválido
   */
  public void setIdleTime() throws Exception {
    // atribui o valor default ao atributo. Caso não ache o elemento no XML este valor sera o válido
    idleTime = DEFAULT_IDLETIME;
    ElementList elementList = confFile.search(XPATH_IDLETIME);
    for (int indexList = 0; indexList < elementList.getSize(); indexList ++)
      idleTime = elementList.getElement(indexList).getValueInt().intValue();
  }

  /**
   * Obtém do arquivo de configurações as informações do diretório no qual os
   * arquivos que não forem processados pelo jFileReceiver serão colocados.
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do rejectedHome for inválido
   */
  public void setRejectedHome() throws Exception {
    rejectedHome.setOutBoxDir(confFile.search(XPATH_REJECDIR + "/" + XPATH_OUTBOXDIR).getElement(0).getValue());
    rejectedHome.setDailyDir(confFile.search(XPATH_REJECDIR + "/" + XPATH_DAILYDIR).getElement(0).getValueBoolean().booleanValue());
  }

  /**
   * Obtém do arquivo de configurações a URL do servlet responsável por obter um
   * novo id
   *
   * @author Gordo&#153;
   * @throws NullPointerException se o xPath para a busca do rejectedHome for inválido
   */
  public void setNewIdUrl() throws Exception {
    newIdUrl = confFile.search(XPATH_NEWIDURL).getElement(0).getValue();
  }

  /**
   * Retorna um inbox da lista de inboxes a ser monitoradas.
   *
   * @author Gordo&#153;
   * @return a inbox equivalente a posição _index na lista de inboxes
   * @param  _index posição da inbox desejada
   * @throws NullPointerException se o xPath para a busca dos inboxes for inválido
   */
  public String getInbox(int _index) throws Exception {
    return (String) inboxes.get(_index);
  }

  /**
   * Obtém do arquivo de configurações o arquivo de log da aplicação
   *
   * @author Gordo&#153;
   * @return path completo do arquivo de log da aplicação
   * @throws NullPointerException se o xPath para a busca do arquivo de log da aplicação for inválido
   */
  public String getApplicationLog() throws Exception {
    return applicationLog;
  }

  /**
   * Obtém do arquivo de configurações o driver para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @return driver para conexão com o banco de dados
   * @throws NullPointerException se o xPath para a busca do driver de conexão for inválido
   */
  public String getDbConnectionDriver() throws Exception {
    return dbConnDriver;
  }

  /**
   * Obtém do arquivo de configurações a url para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @return url para conexão com o banco de dados
   * @throws NullPointerException se o xPath para a busca da url de conexão for inválido
   */
  public String getDbConnectionUrl() throws Exception {
    return dbConnUrl;
  }

  /**
   * Obtém do arquivo de configurações o username para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @return username para conexão com o banco de dados
   * @throws NullPointerException se o xPath para a busca do username de conexão for inválido
   */
  public String getDbConnectionUsername() throws Exception {
    return dbConnUsername;
  }

  /**
   * Obtém do arquivo de configurações a senha para conexão com o banco de dados
   *
   * @author Gordo&#153;
   * @return senha para conexão com o banco de dados
   * @throws NullPointerException se o xPath para a busca da senha de conexão for inválido
   */
  public String getDbConnectionPassword() throws Exception {
    return dbConnPassword;
  }

  /**
   * @return uma instância da classe ValidFile contendo as informações sobre um tipo de arquivo válido
   * @author Gordo&#153;
   */
  public ValidFile getValidFileProperties (int _index) throws Exception {
    return (ValidFile) validFiles.get(_index);
  }

  /**
   * Realiza o refresh do arquivo de configuração.
   *
   * @author Gordo&#153;
   * @throws LockException se o documento especificado já estava travado para escrita ou se houve um erro de I/O ao criar o arquivo lock
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
    // obtendo as configurações para conexão com o banco de dados
    this.setDbConnectionDriver();
    this.setDbConnectionUrl();
    this.setDbConnectionUsername();
    this.setDbConnectionPassword();
    // obtendo os tipos de arquivos válidos e as informações sobre o mesmo
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
   * @return o tamanho da lista de tipos de arquivos válidos
   */
  public int getValidFilesSize () {
    return validFiles.size();
  }

  /**
   * @author Gordo&#153;
   * @return o nível de debug da aplicação
   */
  public int getDebugLevel () {
    return debugLevel;
  }

  /**
   * @author Gordo&#153;
   * @return o sleepTime da aplicação
   */
  public int getSleepTime () {
    return sleepTime;
  }

  /**
   * @author Gordo&#153;
   * @return o nível de debug da aplicação
   */
  public int getIdleTime () {
    return idleTime;
  }

  /**
   * @author Gordo&#153;
   * @return o diretório no qual deverão ser colocado os arquivos que não forem processados pelo jFileReceiver
   */
  public String getRejectedHomeOutBoxDir () throws Exception {
    return rejectedHome.getOutBoxDir();
  }

  /**
   * @author Gordo&#153;
   * @return o diretório principal de arquivos rejeitados
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
   * @return a url do servlet responsável por gerar um novo id
   */
  public String getNewIdUrl () {
    return newIdUrl;
  }

}