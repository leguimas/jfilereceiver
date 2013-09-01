package jfilereceiver.inbox;

import jfilereceiver.general.*;
import jfilereceiver.log.LogMessages;
import jfilereceiver.conf.*;
import jfilereceiver.files.*;
import util.*;
import java.io.*;
import java.util.*;

/**
 * Title:        InboxMonitor
 * Description:  Thread que fica monitorando um diret�rio e verificando se os
 *               arquivos deste dire�rio devem ou n�o serem processados.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class InboxMonitor extends java.lang.Thread {

  // constante que indica qual � o tempo limite para uma thread ficar inativa. Expresso em segundos
  static final int THREAD_OFF_TIME = 120;

  // vari�veis
  LogMessages inboxMonitorLog;
  String homeDir = new String("");
  Configuration jFileReceiverConfiguration;
  ConnectionPool jFileReceiverConnectionPool;
  String today = new String();
  // LinkedList que armazenar� todas as Threads disparadas por este inbox
  LinkedList fileThreads = new LinkedList();
  // vari�vel que indicar� se a thread est� ativa ou n�o. Seu valor � diretamente influenciado
  // pela existencia do arquivo jFileReceiver-conf.pause
  boolean active = true;
  // vari�vel que indicar� se a thread deve parar ou n�o. Seu valor � diretamente influenciado
  // pela exist�ncia do arquivo jFileReceiver-conf.stop
  boolean stop = false;
  File inboxFiles;
  FileTools fileTools = new FileTools();
  // total de threads
  int totalThreads = 0;

  /**
   * Construtor da classe InboxMonitor. Apenas seta uns valores para que as opera��es
   * desta classe possam ser realizadas sem maiores problemas.
   *
   * @author Gordo&#153;
   * @param  _logGenerator instancia da classe utilizada para o log de mensagens
   * @param  _homeDir diret�rio que sera monitorado por esta classe
   * @param  _configuration as configura��es utilizadas pela aplica��o. Ser� utilizada na valida��o dos arquivos.
   * @param  _connectionPool connectionPool a ser utilizado pela aplica��o
   */
  public InboxMonitor(LogMessages _logGenerator, String _homeDir,
                      Configuration _configuration, ConnectionPool _connectionPool) {
    inboxMonitorLog = _logGenerator;
    homeDir = _homeDir;
    jFileReceiverConfiguration = _configuration;
    jFileReceiverConnectionPool = _connectionPool;
    inboxFiles = new File (homeDir);
  }

  /**
   * M�todo que verifica se um diret�rio existe. Caso n�o exista, cria-o.
   *
   * @author Gordo&#153;
   * @param  _directory diret�rio a ser verificado (caminho completo)
   */
  public void createDirectory (String _directory) throws Exception {
    File auxFile = new File (_directory);
    if (auxFile.exists()) {
      if (! auxFile.isDirectory()) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " J� existe o arquivo " + _directory + " mas o mesmo n�o � diret�rio. Diret�rio ser� criado.", inboxMonitorLog.logGenerator.DEBUG);
        if (! auxFile.mkdir()) {
          throw new Exception ("Erro ao se criar o diret�rio " + auxFile.getPath() + ". Verifique se os diret�rios que o antecede existe e se as permiss�es para estes diretorios est�o ok.");
        }
      }
    }
    else
      if (! auxFile.mkdir()) {
        throw new Exception ("Erro ao se criar o diret�rio " + auxFile.getPath() + ". Verifique se os diret�rios que o antecede existe e se as permiss�es para estes diretorios est�o ok.");
      }
  }

  /**
   * M�todo que verifica se os diretorio de arquivos rejeitados e arquivos
   * processados existem. Caso n�o exista, cria os mesmos. Caso encontre algum
   * problema na cria��o, paraliza o jFileReceiver at� a corre��o do problema
   *
   * @author Gordo&#153;
   */
  public void checkDirectories () {
    ValidFile validFile;
    boolean createError = false;
    // verifica se o homeDir � um diret�rio v�lido
    try {
      createDirectory(homeDir);
    }
    catch (Exception processedException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretorio que dever� ser monitorado. " + processedException.toString() + ". Esta thread ser� pausada.", inboxMonitorLog.logGenerator.ERROR);
      createError = true;
    }
    // percorrendo todos os tipos de arquivos v�lidos e verificando os diretorios para os mesmos
    for (int indexValidFiles = 0; indexValidFiles < jFileReceiverConfiguration.getValidFilesSize(); indexValidFiles++ ) {
      try {
        validFile = jFileReceiverConfiguration.getValidFileProperties(indexValidFiles);
        // antes de mais nada verifica se os diret�rios base existem
        try {
          createDirectory(validFile.getProcessedHomeOutBoxNoDate());
          createDirectory(validFile.getProcessedHomeOutBoxDir());
        }
        catch (Exception processedException) {
          inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretorio de arquivos processados. " + processedException.toString() + ". Esta thread ser� pausada.", inboxMonitorLog.logGenerator.ERROR);
          createError = true;
        }
        try {
          createDirectory(validFile.getIrregularHomeOutBoxDirNoDate());
          createDirectory(validFile.getIrregularHomeOutBoxDir());
        }
        catch (Exception irregularException) {
          inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretorio de arquivos irregulares. " + irregularException.toString() + ". O esta thread ser� pausada.", inboxMonitorLog.logGenerator.ERROR);
          createError = true;
        }
      }
      catch (Exception validFileException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] Problemas ao se obter o tipo de arquivo: " + indexValidFiles + " para a cria��o dos subdiret�rios. " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      }
    }
    // valida��o do diret�rio de arquivos que n�o forem processados
    try {
      createDirectory(jFileReceiverConfiguration.getRejectedHomeOutBoxDirNoDate());
      createDirectory(jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
    }
    catch (Exception rejectedException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Problemas ao se obter o diretorio de arquivos rejeitados. " + rejectedException.toString() + ". O esta thread ser� pausada.", inboxMonitorLog.logGenerator.ERROR);
      createError = true;
    }
    // caso ocorreu algum erro durante a cria��o dos diret�rios
    if (createError) {
      try {
        fileTools.createPauseFile(jFileReceiverConfiguration.confDir);
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Devido a problemas na cria��o de diret�rios, o jFileReceiver ser� paralizado. Corrija a situa��o e apague o arquivo jFileReceiver-conf.pause em " + jFileReceiverConfiguration.confDir + " para que o processo funcione corretamente.", inboxMonitorLog.logGenerator.FATAL_ERROR);
      }
      catch (Exception pauseException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Ocorreram problemas na cria��o de diret�rio mas n�o foi possivel criar o arquivo jFileReceiver.pause em " + jFileReceiverConfiguration.confDir + ". Corrija a situa��o, crie o arquivo .pause no diret�rio do arquivo de configura��o e delete-o para o processo voltar a funcionar corretamente.", inboxMonitorLog.logGenerator.FATAL_ERROR );
        this.pauseInboxMonitor(true);
      }
    }
  }

  /**
   * M�todo que cria uma nova thread para um arquivo v�lido recebido por parametro.
   *
   * @author Gordo&#153;
   * @param  _validFile informa��es sobre um arquivo v�lido
   */
  public void newThread(ValidFile _validFile) {
    FileInterface fileInterface;
    try {
      // carregando a classe para processamento do tipo de arquivo
      fileInterface = (FileInterface) Class.forName(_validFile.getFileClassName()).newInstance();
      fileInterface.setLogGenerator(this.inboxMonitorLog);
      fileInterface.setValidFile(_validFile);
      fileInterface.setFilePattern(_validFile.getFilePattern());
      fileInterface.setLogLevel(jFileReceiverConfiguration.getDebugLevel());
      fileInterface.setIdleTime(jFileReceiverConfiguration.getIdleTime());
      fileInterface.setSleepTime(jFileReceiverConfiguration.getSleepTime());
      fileInterface.setNewIdUrl(jFileReceiverConfiguration.getNewIdUrl());
      fileInterface.setRejectedDir(jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
      fileInterface.pauseThread(true);
      fileInterface.startThread();
      // adicionando esta inst�ncia a lista de threads
      fileThreads.add(fileInterface);
      this.totalThreads++;
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Criada mais uma thread para o processamento de arquivos " + _validFile.getFilePattern() + ".", inboxMonitorLog.logGenerator.DEBUG);
    }
    catch (Exception fileClassException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na tentativa de instanciar a classe para processamento do arquivo: " + _validFile.getFilePattern() + ". " + fileClassException.toString(), inboxMonitorLog.logGenerator.ERROR);
    }
  }

  /**
   * M�todo que cria o n�mero m�nimo de threads para cada tipo de arquivo.
   *
   * @author Gordo&#153;
   */
  public void startMinThreads () {
    ValidFile validFile;
    // percorrendo os tipos de arquivos v�lidos
    for (int indexFiles = 0; indexFiles < jFileReceiverConfiguration.getValidFilesSize(); indexFiles ++) {
      // obtendo um tipo de arquivo v�lido
      try {
        validFile = jFileReceiverConfiguration.getValidFileProperties(indexFiles);
        // criando o n�mero m�nimo de threads para este tipo de arquivo
        for (int indexThreads = 0; indexThreads < validFile.getMinThreads(); indexThreads ++) {
          this.newThread(validFile);
        }
      }
      catch (Exception validFileException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obten��o do tipo de arquivo " + indexFiles + ". " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      }
    }
  }

  /**
   * M�todo que obt�m qual thread deve ser utilizada para o processamento de um
   * arquivo. Veirifica se j� existe uma thread dispon�vel para o tipo de arquivo.
   * Caso n�o exista, cria uma nova thread desde que n�o se extrapole o n�mero
   * m�ximo de threads para o tipo de arquivo em quest�o.
   *
   * @author Gordo&#153;
   * @param  _validFileIndex indice do tipo de arquivo ao qual o arquivo se refere
   * @return �ndice referente a Thread a ser utilizada ou -1 caso n�o seja possivel processar o arquivo neste momento
   */
  public int getIndexThread (int _validFileIndex) {
    int result = -1;
    FileInterface fileInterface;
    int amountType = 0;
    try {
      // obtem as informa��es sobre o tipo de arquivo desejado
      ValidFile validFile = jFileReceiverConfiguration.getValidFileProperties(_validFileIndex);
      // percorre a lista de threads para tentar encontrar uma thread a ser utilizada
      for (int indexThreads = 0; (result == -1) && (indexThreads < fileThreads.size()); indexThreads ++) {
        try {
          fileInterface = (FileInterface) fileThreads.get(indexThreads);
          // se a thread for do tipo procurado
          if (fileInterface.getFilePattern().equals(validFile.getFilePattern())) {
            amountType ++;
            // caso a thread esteja ativa a mesma n�o pode ser utilizada
            if (! fileInterface.isActive())
              result = indexThreads;
          }
        }
        catch (Exception fileInterfaceException) {
          inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obten��o da thread " + indexThreads + ". " + fileInterfaceException.toString(), inboxMonitorLog.logGenerator.ERROR);
        }
      }
      // verifica se h� a necessidade de se criar uma nova thread (n�o encontrou nenhuma thread dispon�vel)
      if (result == -1) {
        // veirifica se j� n�o h� o n�mero m�ximo de threads criadas para este tipo de arquivo
        if (amountType < validFile.getMaxThreads()) {
          this.newThread(validFile);
          result = fileThreads.size() - 1;
        }
      }
    }
    catch (Exception validFileException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obten��o do tipo de arquivo " + _validFileIndex + ". " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      return result;
    }
    return result;
  }

  /**
   * M�todo que faz as configura��es necess�rias e starta o processo para processamento
   * de um arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser processado
   * @param  _indexThread �ndice da thread a ser utilizada para o processamento
   * @param  _indexValidFile �ndice do tipo de arquivo v�lido referente a este arquivo
   */
  public void fileProcess (File _file, int _indexThread, ValidFile _validFile) {
    boolean error = false;
    // obtenado a interface referente a thread a ser utilizada
    FileInterface fileInterface = (FileInterface) fileThreads.get(_indexThread);
    // setando alguns par�metros
    fileInterface.setConnectionPool(this.jFileReceiverConnectionPool);
    fileInterface.setLogGenerator(this.inboxMonitorLog);
    fileInterface.setValidFile(_validFile);
    fileInterface.setFile(_file);
    try {
      fileInterface.setRejectedDir(jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
      fileInterface.setIrregularDir(_validFile.getIrregularHomeOutBoxDir());
    }
    catch (Exception fileProcessException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diret�rio de arquivos irregulares. " + fileProcessException.toString() + ". O arquivo " + _file.getPath() + " ser� deslocado.", inboxMonitorLog.logGenerator.ERROR);
      error = true;
    }
    // se n�o ocorreu erro
    if (! error)
      // starta o processo
      fileInterface.pauseThread(false);
    else
      // unlocka o arquivo
      try {
        fileTools.unlockFile(_file, false, true, _validFile.getOverwriteFiles());
      }
      catch (Exception unlockException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao deslockar o arquivo " + _file.getPath() + ". " + unlockException.toString() + ". O mesmo ficar� lockado.", inboxMonitorLog.logGenerator.ERROR);
      }
  }

  /**
   * M�todo run da Thread. Esta thread fica monitorando um diret�rio analisando
   * todos os arquivos que chegam neste diret�rio. Encontrado um arquivo o mesmo
   * � analisado verificando se existe alguma configura��o em configuration que
   * satisfa�a o seu nome / formato. Caso exista alguma configura��o o mesmo �
   * encaminhando para uma outra classe que realiza o processamento do mesmo. Caso
   * n�o seja encontrado nenhuma referencia nas configura��es, o arquivo � movido
   * para a pasta "rejected" dentro do diretorio no qual a Thread est� rodando.
   *
   * @author Gordo&#153;
   * @param  _stop true se a thread deve ser finalizada ou false caso contr�rio
   */
  public void run () {
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Iniciando a monitora��o do diret�rio", inboxMonitorLog.logGenerator.STATUS);
    // startando o m�nimo de Threads para cada tipo de arquivo
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Startando as threads m�nimas.", inboxMonitorLog.logGenerator.STATUS);
    this.startMinThreads();
    // verificando a existencia do diretorio de arquivos processados e rejeitados
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Verificando subdiret�rios necess�rios para o processamento das informa��es", inboxMonitorLog.logGenerator.DEBUG);
    this.checkDirectories();
    // filtro para obter todos os arquivos do diret�rio
    util.FileFilter fileFilter = new util.FileFilter("*.*");
    File[] filesList;
    // enquanto a Thread n�o for setada para parar.
    while (! stop) {
      // se a Thread estiver ativa. Esta situa��o pode ser mudada pela existencia do arquivo jFileReceiver-conf.pause
      if (active) {
        // verificando a existencia do diretorio de arquivos processados e rejeitados
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Verificando subdiret�rios necess�rios para o processamento das informa��es", inboxMonitorLog.logGenerator.DEBUG);
        this.checkDirectories();
        try {
          // obtendo os arquivos (*.*) do diret�rio em quest�o
          filesList = inboxFiles.listFiles(fileFilter);
          File auxFile;
          String originalName = new String("");
          // para cada arquivo encontrado, verifica se o mesmo � v�lido ou n�o
          for (int indexFiles = 0; indexFiles < filesList.length; indexFiles ++) {
            // verifica se o arquivo atual � um diret�rio
            if (! filesList[indexFiles].isDirectory()) {
              // se o arquivo atual n�o estiver sendo lockado
              if (! fileTools.isLocked(filesList[indexFiles])) {
                inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Lockando o arquivo: " + filesList[indexFiles].getName(), inboxMonitorLog.logGenerator.DEBUG);
                originalName = filesList[indexFiles].getName();
                // locka o arquivo para evitar que outros processos o manipulem
                filesList[indexFiles] = fileTools.lockFile(filesList[indexFiles], false, false);
                // verifica se o arquivo � v�lido, ou seja, se h� alguma configura��o para o tipo dele
                boolean fileIsValid = false;
                ValidFile validFile = new ValidFile();
                int fileValidIndex;
                for (fileValidIndex = 0;
                     (fileValidIndex < jFileReceiverConfiguration.getValidFilesSize()) && (! fileIsValid);
                     fileValidIndex ++) {
                  validFile = jFileReceiverConfiguration.getValidFileProperties(fileValidIndex);
                  fileIsValid = fileTools.isValid(originalName, validFile.getFilePattern());
                }
                // se o arquivo for inv�lido, move-o para a pasta de arquivos rejeitados e deslocka-o
                if (!fileIsValid) {
                  try {
                    filesList[indexFiles] = fileTools.moveFile(filesList[indexFiles], jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
                    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " N�o foi encontrado nenhum tipo de arquivo v�lido para o arquivo " + originalName + ". Removido para a pasta " + jFileReceiverConfiguration.getRejectedHomeOutBoxDir() + ".", inboxMonitorLog.logGenerator.DEBUG);
                    filesList[indexFiles] = fileTools.unlockFile(filesList[indexFiles], false, true, false);
                  }
                  catch (Exception fileException) {
                    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro ao se manipular o arquivo " + filesList[indexFiles].getName() + ". Caso o arquivo permane�a lockado, deslocke o manualmente.", inboxMonitorLog.logGenerator.ATTENTION);
                  }
                }
                else {
                  // acerta o �ndice referente ao validFile
                  fileValidIndex --;
                  inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + originalName + " � um aquivo v�lido pelo tipo de arquivo " + fileValidIndex  + ".", inboxMonitorLog.logGenerator.DEBUG);
                  // obtendo a thread a ser utilizada para o processamento deste arquivo
                  validFile = jFileReceiverConfiguration.getValidFileProperties(fileValidIndex);
                  int indexThread = this.getIndexThread(fileValidIndex);
                  // se indexThread for menor que 0 significa que n�o h� nenhuma thread dispon�vel no momento para processar o arquivo
                  if (indexThread < 0)
                    filesList[indexFiles] = fileTools.unlockFile(filesList[indexFiles], false, true, false);
                  else
                    this.fileProcess (filesList[indexFiles], indexThread, validFile);
                }
              }
            }
          }
          // verifica se tem alguma thread "a toa" ha muito tempo e a elimina
          this.checkThreads();
        }
        catch (Exception ex) {
          inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " " + ex.toString(), inboxMonitorLog.logGenerator.ERROR);
        }
      }
      // sleep para evitar 100% de CPU
      try {
        Thread.sleep(jFileReceiverConfiguration.getSleepTime());
      }
      catch (Exception sleepException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Problemas no sleep da thread InboxMonitor: " + sleepException.toString(), inboxMonitorLog.logGenerator.ATTENTION);
      }
    }
  }

  /**
   * M�todo respons�vel por finalizar esta thread e as threads geradas por ela.
   * Esta Thread fica rodando at� que a propriedade stop seja igual a true.
   *
   * @author Gordo&#153;
   */
  public void stopInboxMonitor() {
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Iniciando o processo para finaliza��o desta thread e suas threads dependentes.", inboxMonitorLog.logGenerator.STATUS);
    // seta a vari�vel que indicar� que esta thread deve ser finalizada
    this.stop = true;
    FileInterface fileInterface;
    // seta todas as threads geradas para serem finalizadas
    for (int indexThread = 0; indexThread < fileThreads.size(); indexThread ++) {
      fileInterface = (FileInterface) fileThreads.get(indexThread);
      fileInterface.stopThread();
    }
    // aguarda at� todas as threads morrerem
    boolean threadsStop = false;
    while (! threadsStop) {
      threadsStop = true;
      for (int indexThread = 0; (threadsStop) && (indexThread < fileThreads.size()); indexThread ++) {
        fileInterface = (FileInterface) fileThreads.get(indexThread);
        threadsStop = ! fileInterface.isThreadAlive();
      }
      // sleep para evitar 100% de CPU
      try {
        Thread.sleep(jFileReceiverConfiguration.getSleepTime());
      }
      catch (Exception sleepException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Problemas com o sleep do stopInboxMonitor. " + sleepException.toString(), inboxMonitorLog.logGenerator.STATUS);
      }
    }
    // todas as threads morreram
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Monitora��o finalizada.", inboxMonitorLog.logGenerator.STATUS);
  }

  /**
   * M�todo respons�vel por pausar ou despausar esta thread e as threads geradas por ela.
   * Toda vez que se encontrar o arquivo de configura��es
   * com a exten��o .pause a thread dever� ficar "pausada". Este m�todo serve para
   * pausar ou despausar a thread.
   *
   * @author Gordo&#153;
   * @param  _pause true se for parr pausar o processo ou false se for para re-iniciar o processo
   */
  public void pauseInboxMonitor(boolean _pause) {
    if (_pause)
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Iniciando o processo para pausar esta thread e suas threads dependentes.", inboxMonitorLog.logGenerator.STATUS);
    else
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Iniciando o processo para re-iniciar desta thread e suas threads dependentes.", inboxMonitorLog.logGenerator.STATUS);
    // seta a vari�vel que indicar� que esta thread deve ser finalizada
    this.active = ! _pause;
    if (_pause)
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Monitora��o de diret�rio pausada. Os processos gerados pelo jFileReceiver ser�o pausados ap�s o t�rmino de seu �ltimo ciclo.", inboxMonitorLog.logGenerator.STATUS);
    else
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Monitora��o de diret�rio re-iniciada.", inboxMonitorLog.logGenerator.STATUS);
  }

  /**
   * M�todo respons�vel por monitorar as threads. Verifica se as threads criadas,
   * al�m do n�mero m�nimo de threads por tipo de arquivo, est�o hibernando por
   * muito tempo. O tempo de limite � a constante THREAD_OFF_TIME.
   */
  public void checkThreads() {
    FileInterface fileInterface;
    ValidFile validFile;
    int amountType = 0;
    // percorre os tipos de arquivos v�lidos e verifica suas respectivas threads
    for (int indexFiles = 0; indexFiles < jFileReceiverConfiguration.getValidFilesSize(); indexFiles ++) {
      try {
        // obtendo as informa��es sobre um tipo de arquivo v�lido
        validFile = jFileReceiverConfiguration.getValidFileProperties(indexFiles);
        amountType = 0;
        // percorrendo as threads para verificar a quantidade do tipo de arquivo em quest�o
        for (int indexThreads = 0; indexThreads < fileThreads.size(); indexThreads ++) {
          try {
            fileInterface = (FileInterface) fileThreads.get(indexThreads);
            // verifica se a thread � do mesmo tipo do tipo de arquivo em quest�o
            if (fileInterface.getFilePattern().equals(validFile.getFilePattern())) {
              amountType ++;
            }
          }
          catch (Exception threadsException) {
            inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obten��o da thread " + indexThreads + ". " + threadsException.toString(), inboxMonitorLog.logGenerator.ERROR);
          }
        }
        // verifica a quantidade de threads encontradas
        if (amountType > validFile.getMinThreads()) {
          int indexThreads = 0;
          // percorrendo as threads para eliminar as threads que n�o tiverem sendo utilizadas
          while (indexThreads < fileThreads.size()) {
            try {
              fileInterface = (FileInterface) fileThreads.get(indexThreads);
              // verifica se a thread � do mesmo tipo do tipo de arquivo em quest�o
              if (fileInterface.getFilePattern().equals(validFile.getFilePattern())) {
                // verifica se a thread n�o est� ativa
                if (! fileInterface.isActive()) {
                  try {
                    Date now = new Date (System.currentTimeMillis());
                    inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Thread " + indexThreads + " Hora atual: " + now.toString(), inboxMonitorLog.logGenerator.DEBUG);
                    long difference = now.getTime() - fileInterface.getLastAccess().getTime();
                    inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Thread " + indexThreads + " �ltimo acesso: " + fileInterface.getLastAccess().toString(), inboxMonitorLog.logGenerator.DEBUG);
                    // se a thread estiver inativa a mais de THREAD_OFF_TIME
                    if ((difference / 1000) > THREAD_OFF_TIME) {
                      // verifica��o para respeitar o n�mero m�nimo de threads
                      if (fileThreads.size() > validFile.getMinThreads()) {
                        // seta a thread para parar
                        fileInterface.stopThread();
                        // aguarda at� a mesma ser realmente finalizada
                        while (fileInterface.isThreadAlive())
                          try {
                            Thread.sleep(jFileReceiverConfiguration.getSleepTime());
                          }
                          catch (Exception sleepException) {
                            inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro no sleep para finalizar thread inativa. " + sleepException.toString(), inboxMonitorLog.logGenerator.ATTENTION);
                          }
                        // exclui a thread da lista de threads
                        fileThreads.remove(indexThreads);
                        this.totalThreads--;
                        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Excluida uma thread de processamento de arquivos do tipo " + validFile.getFilePattern() + ".", inboxMonitorLog.logGenerator.ERROR);
                      }
                      else
                        indexThreads ++;
                    }
                    else
                      indexThreads ++;
                  }
                  catch (Exception dateException) {
                    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro com a manipula��o de datas. " + dateException.toString(), inboxMonitorLog.logGenerator.ERROR);
                  }
                }
                else
                  indexThreads ++;
              }
              else
                indexThreads ++;
            }
            catch (Exception threadsException) {
              inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obten��o da thread " + indexThreads + ". " + threadsException.toString(), inboxMonitorLog.logGenerator.ERROR);
            }
          }
        }
      }
      catch (Exception validFileException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obten��o do tipo de arquivo " + indexFiles + ". " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      }
    }
  }

}