package jfilereceiver;

import jfilereceiver.conf.*;
import jfilereceiver.log.*;
import jfilereceiver.inbox.*;
import jfilereceiver.general.*;
import java.util.LinkedList;
import java.util.Date;
import java.io.File;
import util.ConnectionPool;
import util.FileFilter;

/**
 * Title:        JFileReceiver
 * Description:  Processo respons�vel por monitorar alguns diretorios e com base
 *               em algumas configura��es, colocar o conte�do destes arquivos em
 *               banco de dados.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Gordo&#153;
 * @version      1.0
 */

public class JFileReceiver {

  // classe utilizada para que, caso ocorra um kill do processo, o mesmo tente
  // finalizar as coisas pendentes antes de morrer.
  private static class shutdownHook extends java.lang.Thread {

    JFileReceiver jFileReceiver;
    LogMessages logApplication;

    private shutdownHook (JFileReceiver _jFileReceiver, LogMessages _logApplication) {
      jFileReceiver = _jFileReceiver;
      logApplication = _logApplication;
    }

    public void run () {
      jFileReceiver.stopProcess(logApplication);
    }

  }

  // constantes
  public static final String jFrVersion = "1.00";

  // vari�veis
  private static Configuration configuration = null;
  private static LinkedList inboxThreads = new LinkedList();
  private static ConnectionPool connectionPool = null;
  private static boolean stop = false;

  /**
   * M�todo que instancia a classe de configura��es
   *
   * @author Gordo&#153;
   */
  public void loadConfiguration() {
    try {
      configuration.setProperties();
    }
    catch (Exception confException) {
      System.out.println("[jFileReceiver] Erro na obten��o do arquivo de configura��es. Verifique se o mesmo est� validado por seu respectivo XSD.");
      confException.printStackTrace();
      System.exit(0);
    }
  }

  /**
   * M�todo que instancia a classe de log
   *
   * @author Gordo&#153;
   * @return uma nova instancia da classe LogMessages
   */
  public LogMessages getLogApplication () {
    LogMessages logApplication = null;
    try {
      // inst�ncia a classe de log
      logApplication = new LogMessages(configuration.getApplicationLog(),
                                       configuration.getDebugLevel(),
                                       false);
    }
    catch (Exception logException) {
      System.out.println("[jFileReceiver] Erro ao se instanciar a classe de log.");
      logException.printStackTrace();
      System.exit(0);
    }
    return logApplication;
  }

  /**
   * M�todo que instancia o pool de conex�es com o banco de dados
   *
   * @author Gordo&#153;
   * @param  _logApplication instancia da classe de log
   */
  public void setConnectionPool (LogMessages _logApplication) {
    try {
      connectionPool = new ConnectionPool(configuration.getDbConnectionDriver(),
                                          configuration.getDbConnectionUrl(),
                                          configuration.getDbConnectionUsername(),
                                          configuration.getDbConnectionPassword());
    }
    catch (Exception poolException) {
      _logApplication.log("[jFileReceiver] Erro ao se criar o connectionPool. " + poolException.toString(), _logApplication.logGenerator.FATAL_ERROR);
      System.out.println("[jFileReceiver] Erro ao se criar o connectionPool. Confira o arquivo de log para maiores informa��es.");
      System.exit(0);
    }
  }

  /**
   * M�todo que starta as threads que monitoram os diret�rios.
   *
   * @author Gordo&#153;
   * @param  _logApplication instancia da classe de log
   */
  public void startThreads (LogMessages _logApplication) {
    InboxMonitor inboxMonitor = null;
    for (int inboxIndex = 0; inboxIndex < configuration.getInboxesSize(); inboxIndex ++) {
      try {
        inboxMonitor = new InboxMonitor(_logApplication,
                                        configuration.getInbox(inboxIndex),
                                        configuration,
                                        connectionPool);
      }
      catch (Exception inboxException) {
        _logApplication.log("[jFileReceiver] Erro na obten��o do inbox de �ndice " + inboxIndex + ".", _logApplication.logGenerator.ERROR);
      }
      if (inboxMonitor != null) {
        inboxThreads.add(inboxMonitor);
        inboxMonitor.start();
      }
    }
  }

  /**
   * M�todo que � disparado ao ser encontrado o arquivo de configura��oes com a
   * extens�o .stop. Para todos os processos disparados por esta classe. Vale a
   * pena ressaltar que os processos somente ser�o finalizados ap�s a sua �ltima
   * execu��o completa. Caso um processo ainda esteja sendo executado, ser� aguardado
   * o t�rmino do mesmo para a sua finaliza��o.
   *
   * @author Gordo&#153;
   * @param  _logApplication inst�ncia da classe LogMessages para gerar log das opera��es
   */
  public void stopProcess (LogMessages _logApplication) {
    _logApplication.log("[jFileReceiver] Iniciando processo de finaliza��o do jFileReceiver.", _logApplication.logGenerator.STATUS);
    InboxMonitor inboxMonitor;
    // percorre todos os inboxes setando os mesmos para finalizar
    for (int inboxIndex = 0; inboxIndex < inboxThreads.size(); inboxIndex ++) {
      inboxMonitor = (InboxMonitor) inboxThreads.get(inboxIndex);
      inboxMonitor.stopInboxMonitor();
    }
    _logApplication.log("[jFileReceiver] Aguardando que todas as threads sejam finalizadas.", _logApplication.logGenerator.STATUS);
    // fica aguardando at� todas as threads realmente morrerem
    boolean threadsStop = false;
    while (! threadsStop) {
      threadsStop = true;
      for (int inboxIndex = 0; (threadsStop) && (inboxIndex < inboxThreads.size()); inboxIndex ++) {
        inboxMonitor = (InboxMonitor) inboxThreads.get(inboxIndex);
        threadsStop = ! inboxMonitor.isAlive();
      }
      // sleep para evitar 100% de CPU
      try {
        Thread.sleep(configuration.getSleepTime());
      }
      catch (Exception sleepException) {
        _logApplication.log("[jFileReceiver] Problemas com o sleep do stopProcess. " + sleepException.toString(), _logApplication.logGenerator.ATTENTION);
      }
    }
    // todas as threads morreram
    _logApplication.log("[jFileReceiver] jFileReceiver finalizado.", _logApplication.logGenerator.STATUS);
  }

  /**
   * M�todo que � disparado ao ser encontrado o arquivo de configura��oes com a
   * extens�o .pause. Pausa, dependendo do valor de _pause, o processo e todos os
   * subprocessos disparados por esta classe. Assim como o stopProcess, o processo
   * s� � realmente pausado ap�s a sua �ltima execu��o. Fica neste estado at� a
   * remo��o do arquivo de configura��oes com a extens�o .pause.
   *
   * @author Gordo&#153;
   * @param  _pause true para parar ou false para re-iniciar
   * @param  _logApplication inst�ncia da classe LogMessages para gerar log das opera��es
   */
  public void pauseProcess (boolean _pause, LogMessages _logApplication) {
    if (_pause)
      _logApplication.log("[jFileReceiver] Iniciando processo para pausar o jFileReceiver.", _logApplication.logGenerator.STATUS);
    else
      _logApplication.log("[jFileReceiver] Iniciando o processo para re-iniciar o jFileReceiver.", _logApplication.logGenerator.STATUS);
    InboxMonitor inboxMonitor;
    // percorre todos os inboxes setando os mesmos para finalizar
    for (int inboxIndex = 0; inboxIndex < inboxThreads.size(); inboxIndex ++) {
      inboxMonitor = (InboxMonitor) inboxThreads.get(inboxIndex);
      inboxMonitor.pauseInboxMonitor(_pause);
    }
    if (_pause)
      _logApplication.log("[jFileReceiver] jFileReceiver paralizado.", _logApplication.logGenerator.STATUS);
    else
      _logApplication.log("[jFileReceiver] jFileReceiver reiniciado.", _logApplication.logGenerator.STATUS);
  }

  public static void main(String[] args) {

    JFileReceiver jFileReceiver = new JFileReceiver();

    LogMessages logApplication = null;

    String stopFile;
    String pauseFile;
    String refreshFile;

    // inicializando a classe que obtem as configura��es da aplica��o
    if (args.length > 0) {
      // verifica se o par�metro foi informado
      if (!("".equals(args[0])))
        // instancia a classe de configura��o
        configuration = new Configuration(args[0]);
      else {
        System.out.println("[jFileReceiver] � necess�rio informar o diret�rio no qual o arquivo de configura��es jFileReceiver-conf.xml est� localizado.");
        System.out.println("[jFileReceiver] Utiliza��o: java -jar JFileReceiver <CONF_DIR>");
        System.exit(0);
      }
    }
    else {
      System.out.println("[jFileReceiver] � necess�rio informar o diret�rio no qual o arquivo de configura��es jFileReceiver-conf.xml est� localizado.");
      System.out.println("[jFileReceiver] Utiliza��o: java -jar JFileReceiver <CONF_DIR>");
      System.exit(0);
    }

    // carregando as configura��es da aplica��o
    jFileReceiver.loadConfiguration();

    // setando o arquivo de log
    logApplication = jFileReceiver.getLogApplication();

    logApplication.log("[jFileReceiver] Inicializando jFileReceiver...", logApplication.logGenerator.STATUS);

    // startando o connectionPool
    jFileReceiver.setConnectionPool(logApplication);

    // startando as Threads de monitora��o de inboxes
    jFileReceiver.startThreads(logApplication);

    // evitando que processos terminem durante a sua execu��o
    try {
      Runtime.getRuntime().addShutdownHook(new shutdownHook(jFileReceiver, logApplication));
    }
    catch (Exception hookException) {
      logApplication.log("[jFileReceiver] Problemas ao se setar o shutdownHook na aplica��o. Utilize o .stop caso deseje finalizar o programa. " + hookException.toString(), logApplication.logGenerator.ERROR);
    }

    logApplication.log("[jFileReceiver] jFileReceiver inicializado.", logApplication.logGenerator.STATUS);

    // fica monitorando para encontrar algum arquivo que indique alguma opera��o
    FileFilter fileFilter = new FileFilter (configuration.CONF_FILE_NAME.substring(0, configuration.CONF_FILE_NAME.indexOf(".")) + ".*");
    File[] fileList;
    File configurationFile = new File (configuration.confDir);
    // setando os arquivos que indicam stop, pause e refreh
    stopFile = configuration.CONF_FILE_NAME.substring(0, configuration.CONF_FILE_NAME.indexOf(".")) + ".stop";
    pauseFile = configuration.CONF_FILE_NAME.substring(0, configuration.CONF_FILE_NAME.indexOf(".")) + ".pause";
    refreshFile = configuration.CONF_FILE_NAME.substring(0, configuration.CONF_FILE_NAME.indexOf(".")) + ".refresh";
    // loop da monitora��o
    while (! stop) {
      fileList = configurationFile.listFiles(fileFilter);
      // percorre os arquivos encontrados
      for (int indexFile = 0; indexFile < fileList.length; indexFile++) {
        // finalizando o processo do jFileReceiver
        if (fileList[indexFile].getName().equals(stopFile)) {
          jFileReceiver.stopProcess(logApplication);
          // seta o processo principal para finaliz�-lo
          stop = true;
          if (! fileList[indexFile].delete())
            logApplication.log("[jFileReceiver] Problemas ao se excluir o arquivo " + fileList[indexFile].getName() + ". Apague o arquivo manualmente.", logApplication.logGenerator.ATTENTION);
        }
        // paralizando, temporariamente o processo
        else if (fileList[indexFile].getName().equals(pauseFile)) {
          jFileReceiver.pauseProcess(true, logApplication);
          while (fileList[indexFile].exists())
            // sleep para evitar 100% de CPU
            try {
              Thread.sleep(configuration.getSleepTime());
            }
            catch (Exception pauseException) {
              logApplication.log("[jFileReceiver] Problemas no sleep referente ao arquivo de configura��es com extens�o .pause . " + pauseException.toString(), logApplication.logGenerator.ATTENTION);
            }
          jFileReceiver.pauseProcess(false, logApplication);
        }
        // atualizando as configura��es
        else if (fileList[indexFile].getName().equals(refreshFile)) {
          logApplication.log("[jFileReceiver] Finalizando o jFileReceiver para que as configura��es sejam atualizadas.", logApplication.logGenerator.STATUS);
          jFileReceiver.stopProcess(logApplication);
          configuration = new Configuration(args[0]);
          jFileReceiver.loadConfiguration();
          logApplication = jFileReceiver.getLogApplication();
          jFileReceiver.setConnectionPool(logApplication);
          jFileReceiver.startThreads(logApplication);
          if (! fileList[indexFile].delete())
            logApplication.log("[jFileReceiver] Problemas ao se excluir o arquivo " + fileList[indexFile].getName() + ". Isso poder� causar um loop. Apague o arquivo manualmente.", logApplication.logGenerator.ATTENTION);
        }
      }
      // sleep para evitar 100% de CPU
      try {
        Thread.sleep(configuration.getSleepTime());
      }
      catch (Exception sleepException) {
        logApplication.log("[jFileReceiver] Problemas no sleep da monitora��o dos arquivos de configura��o. " + sleepException.toString(), logApplication.logGenerator.ATTENTION);
      }
    }
    System.gc();
    System.exit(0);
  }

}