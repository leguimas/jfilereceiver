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
 * Description:  Thread que fica monitorando um diretório e verificando se os
 *               arquivos deste direório devem ou não serem processados.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class InboxMonitor extends java.lang.Thread {

  // constante que indica qual é o tempo limite para uma thread ficar inativa. Expresso em segundos
  static final int THREAD_OFF_TIME = 120;

  // variáveis
  LogMessages inboxMonitorLog;
  String homeDir = new String("");
  Configuration jFileReceiverConfiguration;
  ConnectionPool jFileReceiverConnectionPool;
  String today = new String();
  // LinkedList que armazenará todas as Threads disparadas por este inbox
  LinkedList fileThreads = new LinkedList();
  // variável que indicará se a thread está ativa ou não. Seu valor é diretamente influenciado
  // pela existencia do arquivo jFileReceiver-conf.pause
  boolean active = true;
  // variável que indicará se a thread deve parar ou não. Seu valor é diretamente influenciado
  // pela existência do arquivo jFileReceiver-conf.stop
  boolean stop = false;
  File inboxFiles;
  FileTools fileTools = new FileTools();
  // total de threads
  int totalThreads = 0;

  /**
   * Construtor da classe InboxMonitor. Apenas seta uns valores para que as operações
   * desta classe possam ser realizadas sem maiores problemas.
   *
   * @author Gordo&#153;
   * @param  _logGenerator instancia da classe utilizada para o log de mensagens
   * @param  _homeDir diretório que sera monitorado por esta classe
   * @param  _configuration as configurações utilizadas pela aplicação. Será utilizada na validação dos arquivos.
   * @param  _connectionPool connectionPool a ser utilizado pela aplicação
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
   * Método que verifica se um diretório existe. Caso não exista, cria-o.
   *
   * @author Gordo&#153;
   * @param  _directory diretório a ser verificado (caminho completo)
   */
  public void createDirectory (String _directory) throws Exception {
    File auxFile = new File (_directory);
    if (auxFile.exists()) {
      if (! auxFile.isDirectory()) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Já existe o arquivo " + _directory + " mas o mesmo não é diretório. Diretório será criado.", inboxMonitorLog.logGenerator.DEBUG);
        if (! auxFile.mkdir()) {
          throw new Exception ("Erro ao se criar o diretório " + auxFile.getPath() + ". Verifique se os diretórios que o antecede existe e se as permissões para estes diretorios estão ok.");
        }
      }
    }
    else
      if (! auxFile.mkdir()) {
        throw new Exception ("Erro ao se criar o diretório " + auxFile.getPath() + ". Verifique se os diretórios que o antecede existe e se as permissões para estes diretorios estão ok.");
      }
  }

  /**
   * Método que verifica se os diretorio de arquivos rejeitados e arquivos
   * processados existem. Caso não exista, cria os mesmos. Caso encontre algum
   * problema na criação, paraliza o jFileReceiver até a correção do problema
   *
   * @author Gordo&#153;
   */
  public void checkDirectories () {
    ValidFile validFile;
    boolean createError = false;
    // verifica se o homeDir é um diretório válido
    try {
      createDirectory(homeDir);
    }
    catch (Exception processedException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretorio que deverá ser monitorado. " + processedException.toString() + ". Esta thread será pausada.", inboxMonitorLog.logGenerator.ERROR);
      createError = true;
    }
    // percorrendo todos os tipos de arquivos válidos e verificando os diretorios para os mesmos
    for (int indexValidFiles = 0; indexValidFiles < jFileReceiverConfiguration.getValidFilesSize(); indexValidFiles++ ) {
      try {
        validFile = jFileReceiverConfiguration.getValidFileProperties(indexValidFiles);
        // antes de mais nada verifica se os diretórios base existem
        try {
          createDirectory(validFile.getProcessedHomeOutBoxNoDate());
          createDirectory(validFile.getProcessedHomeOutBoxDir());
        }
        catch (Exception processedException) {
          inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretorio de arquivos processados. " + processedException.toString() + ". Esta thread será pausada.", inboxMonitorLog.logGenerator.ERROR);
          createError = true;
        }
        try {
          createDirectory(validFile.getIrregularHomeOutBoxDirNoDate());
          createDirectory(validFile.getIrregularHomeOutBoxDir());
        }
        catch (Exception irregularException) {
          inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretorio de arquivos irregulares. " + irregularException.toString() + ". O esta thread será pausada.", inboxMonitorLog.logGenerator.ERROR);
          createError = true;
        }
      }
      catch (Exception validFileException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] Problemas ao se obter o tipo de arquivo: " + indexValidFiles + " para a criação dos subdiretórios. " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      }
    }
    // validação do diretório de arquivos que não forem processados
    try {
      createDirectory(jFileReceiverConfiguration.getRejectedHomeOutBoxDirNoDate());
      createDirectory(jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
    }
    catch (Exception rejectedException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Problemas ao se obter o diretorio de arquivos rejeitados. " + rejectedException.toString() + ". O esta thread será pausada.", inboxMonitorLog.logGenerator.ERROR);
      createError = true;
    }
    // caso ocorreu algum erro durante a criação dos diretórios
    if (createError) {
      try {
        fileTools.createPauseFile(jFileReceiverConfiguration.confDir);
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Devido a problemas na criação de diretórios, o jFileReceiver será paralizado. Corrija a situação e apague o arquivo jFileReceiver-conf.pause em " + jFileReceiverConfiguration.confDir + " para que o processo funcione corretamente.", inboxMonitorLog.logGenerator.FATAL_ERROR);
      }
      catch (Exception pauseException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Ocorreram problemas na criação de diretório mas não foi possivel criar o arquivo jFileReceiver.pause em " + jFileReceiverConfiguration.confDir + ". Corrija a situação, crie o arquivo .pause no diretório do arquivo de configuração e delete-o para o processo voltar a funcionar corretamente.", inboxMonitorLog.logGenerator.FATAL_ERROR );
        this.pauseInboxMonitor(true);
      }
    }
  }

  /**
   * Método que cria uma nova thread para um arquivo válido recebido por parametro.
   *
   * @author Gordo&#153;
   * @param  _validFile informações sobre um arquivo válido
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
      // adicionando esta instância a lista de threads
      fileThreads.add(fileInterface);
      this.totalThreads++;
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Criada mais uma thread para o processamento de arquivos " + _validFile.getFilePattern() + ".", inboxMonitorLog.logGenerator.DEBUG);
    }
    catch (Exception fileClassException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na tentativa de instanciar a classe para processamento do arquivo: " + _validFile.getFilePattern() + ". " + fileClassException.toString(), inboxMonitorLog.logGenerator.ERROR);
    }
  }

  /**
   * Método que cria o número mínimo de threads para cada tipo de arquivo.
   *
   * @author Gordo&#153;
   */
  public void startMinThreads () {
    ValidFile validFile;
    // percorrendo os tipos de arquivos válidos
    for (int indexFiles = 0; indexFiles < jFileReceiverConfiguration.getValidFilesSize(); indexFiles ++) {
      // obtendo um tipo de arquivo válido
      try {
        validFile = jFileReceiverConfiguration.getValidFileProperties(indexFiles);
        // criando o número mínimo de threads para este tipo de arquivo
        for (int indexThreads = 0; indexThreads < validFile.getMinThreads(); indexThreads ++) {
          this.newThread(validFile);
        }
      }
      catch (Exception validFileException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obtenção do tipo de arquivo " + indexFiles + ". " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      }
    }
  }

  /**
   * Método que obtém qual thread deve ser utilizada para o processamento de um
   * arquivo. Veirifica se já existe uma thread disponível para o tipo de arquivo.
   * Caso não exista, cria uma nova thread desde que não se extrapole o número
   * máximo de threads para o tipo de arquivo em questão.
   *
   * @author Gordo&#153;
   * @param  _validFileIndex indice do tipo de arquivo ao qual o arquivo se refere
   * @return índice referente a Thread a ser utilizada ou -1 caso não seja possivel processar o arquivo neste momento
   */
  public int getIndexThread (int _validFileIndex) {
    int result = -1;
    FileInterface fileInterface;
    int amountType = 0;
    try {
      // obtem as informações sobre o tipo de arquivo desejado
      ValidFile validFile = jFileReceiverConfiguration.getValidFileProperties(_validFileIndex);
      // percorre a lista de threads para tentar encontrar uma thread a ser utilizada
      for (int indexThreads = 0; (result == -1) && (indexThreads < fileThreads.size()); indexThreads ++) {
        try {
          fileInterface = (FileInterface) fileThreads.get(indexThreads);
          // se a thread for do tipo procurado
          if (fileInterface.getFilePattern().equals(validFile.getFilePattern())) {
            amountType ++;
            // caso a thread esteja ativa a mesma não pode ser utilizada
            if (! fileInterface.isActive())
              result = indexThreads;
          }
        }
        catch (Exception fileInterfaceException) {
          inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obtenção da thread " + indexThreads + ". " + fileInterfaceException.toString(), inboxMonitorLog.logGenerator.ERROR);
        }
      }
      // verifica se há a necessidade de se criar uma nova thread (não encontrou nenhuma thread disponível)
      if (result == -1) {
        // veirifica se já não há o número máximo de threads criadas para este tipo de arquivo
        if (amountType < validFile.getMaxThreads()) {
          this.newThread(validFile);
          result = fileThreads.size() - 1;
        }
      }
    }
    catch (Exception validFileException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obtenção do tipo de arquivo " + _validFileIndex + ". " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      return result;
    }
    return result;
  }

  /**
   * Método que faz as configurações necessárias e starta o processo para processamento
   * de um arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser processado
   * @param  _indexThread índice da thread a ser utilizada para o processamento
   * @param  _indexValidFile índice do tipo de arquivo válido referente a este arquivo
   */
  public void fileProcess (File _file, int _indexThread, ValidFile _validFile) {
    boolean error = false;
    // obtenado a interface referente a thread a ser utilizada
    FileInterface fileInterface = (FileInterface) fileThreads.get(_indexThread);
    // setando alguns parâmetros
    fileInterface.setConnectionPool(this.jFileReceiverConnectionPool);
    fileInterface.setLogGenerator(this.inboxMonitorLog);
    fileInterface.setValidFile(_validFile);
    fileInterface.setFile(_file);
    try {
      fileInterface.setRejectedDir(jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
      fileInterface.setIrregularDir(_validFile.getIrregularHomeOutBoxDir());
    }
    catch (Exception fileProcessException) {
      inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao se obter o diretório de arquivos irregulares. " + fileProcessException.toString() + ". O arquivo " + _file.getPath() + " será deslocado.", inboxMonitorLog.logGenerator.ERROR);
      error = true;
    }
    // se não ocorreu erro
    if (! error)
      // starta o processo
      fileInterface.pauseThread(false);
    else
      // unlocka o arquivo
      try {
        fileTools.unlockFile(_file, false, true, _validFile.getOverwriteFiles());
      }
      catch (Exception unlockException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Problemas ao deslockar o arquivo " + _file.getPath() + ". " + unlockException.toString() + ". O mesmo ficará lockado.", inboxMonitorLog.logGenerator.ERROR);
      }
  }

  /**
   * Método run da Thread. Esta thread fica monitorando um diretório analisando
   * todos os arquivos que chegam neste diretório. Encontrado um arquivo o mesmo
   * é analisado verificando se existe alguma configuração em configuration que
   * satisfaça o seu nome / formato. Caso exista alguma configuração o mesmo é
   * encaminhando para uma outra classe que realiza o processamento do mesmo. Caso
   * não seja encontrado nenhuma referencia nas configurações, o arquivo é movido
   * para a pasta "rejected" dentro do diretorio no qual a Thread está rodando.
   *
   * @author Gordo&#153;
   * @param  _stop true se a thread deve ser finalizada ou false caso contrário
   */
  public void run () {
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Iniciando a monitoração do diretório", inboxMonitorLog.logGenerator.STATUS);
    // startando o mínimo de Threads para cada tipo de arquivo
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Startando as threads mínimas.", inboxMonitorLog.logGenerator.STATUS);
    this.startMinThreads();
    // verificando a existencia do diretorio de arquivos processados e rejeitados
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Verificando subdiretórios necessários para o processamento das informações", inboxMonitorLog.logGenerator.DEBUG);
    this.checkDirectories();
    // filtro para obter todos os arquivos do diretório
    util.FileFilter fileFilter = new util.FileFilter("*.*");
    File[] filesList;
    // enquanto a Thread não for setada para parar.
    while (! stop) {
      // se a Thread estiver ativa. Esta situação pode ser mudada pela existencia do arquivo jFileReceiver-conf.pause
      if (active) {
        // verificando a existencia do diretorio de arquivos processados e rejeitados
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Verificando subdiretórios necessários para o processamento das informações", inboxMonitorLog.logGenerator.DEBUG);
        this.checkDirectories();
        try {
          // obtendo os arquivos (*.*) do diretório em questão
          filesList = inboxFiles.listFiles(fileFilter);
          File auxFile;
          String originalName = new String("");
          // para cada arquivo encontrado, verifica se o mesmo é válido ou não
          for (int indexFiles = 0; indexFiles < filesList.length; indexFiles ++) {
            // verifica se o arquivo atual é um diretório
            if (! filesList[indexFiles].isDirectory()) {
              // se o arquivo atual não estiver sendo lockado
              if (! fileTools.isLocked(filesList[indexFiles])) {
                inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Lockando o arquivo: " + filesList[indexFiles].getName(), inboxMonitorLog.logGenerator.DEBUG);
                originalName = filesList[indexFiles].getName();
                // locka o arquivo para evitar que outros processos o manipulem
                filesList[indexFiles] = fileTools.lockFile(filesList[indexFiles], false, false);
                // verifica se o arquivo é válido, ou seja, se há alguma configuração para o tipo dele
                boolean fileIsValid = false;
                ValidFile validFile = new ValidFile();
                int fileValidIndex;
                for (fileValidIndex = 0;
                     (fileValidIndex < jFileReceiverConfiguration.getValidFilesSize()) && (! fileIsValid);
                     fileValidIndex ++) {
                  validFile = jFileReceiverConfiguration.getValidFileProperties(fileValidIndex);
                  fileIsValid = fileTools.isValid(originalName, validFile.getFilePattern());
                }
                // se o arquivo for inválido, move-o para a pasta de arquivos rejeitados e deslocka-o
                if (!fileIsValid) {
                  try {
                    filesList[indexFiles] = fileTools.moveFile(filesList[indexFiles], jFileReceiverConfiguration.getRejectedHomeOutBoxDir());
                    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Não foi encontrado nenhum tipo de arquivo válido para o arquivo " + originalName + ". Removido para a pasta " + jFileReceiverConfiguration.getRejectedHomeOutBoxDir() + ".", inboxMonitorLog.logGenerator.DEBUG);
                    filesList[indexFiles] = fileTools.unlockFile(filesList[indexFiles], false, true, false);
                  }
                  catch (Exception fileException) {
                    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro ao se manipular o arquivo " + filesList[indexFiles].getName() + ". Caso o arquivo permaneça lockado, deslocke o manualmente.", inboxMonitorLog.logGenerator.ATTENTION);
                  }
                }
                else {
                  // acerta o índice referente ao validFile
                  fileValidIndex --;
                  inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + originalName + " é um aquivo válido pelo tipo de arquivo " + fileValidIndex  + ".", inboxMonitorLog.logGenerator.DEBUG);
                  // obtendo a thread a ser utilizada para o processamento deste arquivo
                  validFile = jFileReceiverConfiguration.getValidFileProperties(fileValidIndex);
                  int indexThread = this.getIndexThread(fileValidIndex);
                  // se indexThread for menor que 0 significa que não há nenhuma thread disponível no momento para processar o arquivo
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
   * Método responsável por finalizar esta thread e as threads geradas por ela.
   * Esta Thread fica rodando até que a propriedade stop seja igual a true.
   *
   * @author Gordo&#153;
   */
  public void stopInboxMonitor() {
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Iniciando o processo para finalização desta thread e suas threads dependentes.", inboxMonitorLog.logGenerator.STATUS);
    // seta a variável que indicará que esta thread deve ser finalizada
    this.stop = true;
    FileInterface fileInterface;
    // seta todas as threads geradas para serem finalizadas
    for (int indexThread = 0; indexThread < fileThreads.size(); indexThread ++) {
      fileInterface = (FileInterface) fileThreads.get(indexThread);
      fileInterface.stopThread();
    }
    // aguarda até todas as threads morrerem
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
    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Monitoração finalizada.", inboxMonitorLog.logGenerator.STATUS);
  }

  /**
   * Método responsável por pausar ou despausar esta thread e as threads geradas por ela.
   * Toda vez que se encontrar o arquivo de configurações
   * com a extenção .pause a thread deverá ficar "pausada". Este método serve para
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
    // seta a variável que indicará que esta thread deve ser finalizada
    this.active = ! _pause;
    if (_pause)
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Monitoração de diretório pausada. Os processos gerados pelo jFileReceiver serão pausados após o término de seu último ciclo.", inboxMonitorLog.logGenerator.STATUS);
    else
      inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Monitoração de diretório re-iniciada.", inboxMonitorLog.logGenerator.STATUS);
  }

  /**
   * Método responsável por monitorar as threads. Verifica se as threads criadas,
   * além do número mínimo de threads por tipo de arquivo, estão hibernando por
   * muito tempo. O tempo de limite é a constante THREAD_OFF_TIME.
   */
  public void checkThreads() {
    FileInterface fileInterface;
    ValidFile validFile;
    int amountType = 0;
    // percorre os tipos de arquivos válidos e verifica suas respectivas threads
    for (int indexFiles = 0; indexFiles < jFileReceiverConfiguration.getValidFilesSize(); indexFiles ++) {
      try {
        // obtendo as informações sobre um tipo de arquivo válido
        validFile = jFileReceiverConfiguration.getValidFileProperties(indexFiles);
        amountType = 0;
        // percorrendo as threads para verificar a quantidade do tipo de arquivo em questão
        for (int indexThreads = 0; indexThreads < fileThreads.size(); indexThreads ++) {
          try {
            fileInterface = (FileInterface) fileThreads.get(indexThreads);
            // verifica se a thread é do mesmo tipo do tipo de arquivo em questão
            if (fileInterface.getFilePattern().equals(validFile.getFilePattern())) {
              amountType ++;
            }
          }
          catch (Exception threadsException) {
            inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obtenção da thread " + indexThreads + ". " + threadsException.toString(), inboxMonitorLog.logGenerator.ERROR);
          }
        }
        // verifica a quantidade de threads encontradas
        if (amountType > validFile.getMinThreads()) {
          int indexThreads = 0;
          // percorrendo as threads para eliminar as threads que não tiverem sendo utilizadas
          while (indexThreads < fileThreads.size()) {
            try {
              fileInterface = (FileInterface) fileThreads.get(indexThreads);
              // verifica se a thread é do mesmo tipo do tipo de arquivo em questão
              if (fileInterface.getFilePattern().equals(validFile.getFilePattern())) {
                // verifica se a thread não está ativa
                if (! fileInterface.isActive()) {
                  try {
                    Date now = new Date (System.currentTimeMillis());
                    inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Thread " + indexThreads + " Hora atual: " + now.toString(), inboxMonitorLog.logGenerator.DEBUG);
                    long difference = now.getTime() - fileInterface.getLastAccess().getTime();
                    inboxMonitorLog.log("[jFileReceiver.inbox.inboxMonitor] " + homeDir + " Thread " + indexThreads + " Último acesso: " + fileInterface.getLastAccess().toString(), inboxMonitorLog.logGenerator.DEBUG);
                    // se a thread estiver inativa a mais de THREAD_OFF_TIME
                    if ((difference / 1000) > THREAD_OFF_TIME) {
                      // verificação para respeitar o número mínimo de threads
                      if (fileThreads.size() > validFile.getMinThreads()) {
                        // seta a thread para parar
                        fileInterface.stopThread();
                        // aguarda até a mesma ser realmente finalizada
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
                    inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro com a manipulação de datas. " + dateException.toString(), inboxMonitorLog.logGenerator.ERROR);
                  }
                }
                else
                  indexThreads ++;
              }
              else
                indexThreads ++;
            }
            catch (Exception threadsException) {
              inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obtenção da thread " + indexThreads + ". " + threadsException.toString(), inboxMonitorLog.logGenerator.ERROR);
            }
          }
        }
      }
      catch (Exception validFileException) {
        inboxMonitorLog.log("[jFileReceiver.inbox.InboxMonitor] " + homeDir + " Erro na obtenção do tipo de arquivo " + indexFiles + ". " + validFileException.toString(), inboxMonitorLog.logGenerator.ERROR);
      }
    }
  }

}