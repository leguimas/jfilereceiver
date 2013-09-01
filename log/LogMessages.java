package jfilereceiver.log;

import util.LogGenerator;
import jfilereceiver.general.DataTools;

/**
 * Title:        LogMessages
 * Description:  Classe que deixa mais transparente a operação de log de mensagens
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class LogMessages {

  // constantes
  static final int MAX_TRY = 5;

  // variáveis
  public LogGenerator logGenerator;
  int logLevel;
  String lastDate;
  boolean defaultOut;
  String fileName;

  /**
   * Construtor da classe. Apenas instancia a classe LogGenerator de acordo com
   * as informações recebidas.
   *
   * @author Gordo&#153;
   * @param  _fileName nome do arquivo de log
   * @param  _logLevel nível de log (ATTENTION, DEBUG, STATUS, etc) máximo
   * @param  _defaultOut booleana que indica se as mensagens deve ser logadas na saída padrão
   */
  public LogMessages(String _fileName, int _logLevel, boolean _defaultOut ) throws Exception {
    // setando variáveis para futura utilização
    fileName = _fileName;
    logLevel = _logLevel;
    defaultOut = _defaultOut;
    // montando o nome do arquivo de log
    _fileName = _fileName.substring(0, _fileName.lastIndexOf(".") + 1) +
                DataTools.getSysdate("yyyyMMdd") +
                _fileName.substring(_fileName.lastIndexOf("."), _fileName.length());
    //
    lastDate = DataTools.getSysdate("yyyyMMdd");
    // instanciando a classe de log
    logGenerator = new LogGenerator (_fileName, _logLevel, _defaultOut);
  }

  /**
   * Método que realiza o processo de logar uma operação através da classe LogGenerator
   * mas com o try catch "embutido". No caso de problemas com o LogGenerator, o método
   * loga a mensagem na saida padrão (System.out.println()).
   *
   * @author Gordo&#153;
   * @param  _message mensagem a ser logada
   * @param  _logLevel nível do log (ATTENTION, DEBUG, STATUS, etc)
   */
  public void log (String _message, int _logLevel) {
    boolean sucess = false;
    int amountTry = 0;
    // verificando se deve-se gerar um novo log
    try {
      if (!(lastDate.equals(DataTools.getSysdate("yyyyMMdd")))) {
        String newLog = fileName.substring(0, fileName.lastIndexOf(".") + 1) +
                        DataTools.getSysdate("yyyyMMdd") +
                        fileName.substring(fileName.lastIndexOf("."), fileName.length());
        // instancia o log com a nova data
        logGenerator = new LogGenerator (newLog, logLevel, defaultOut);
        lastDate = DataTools.getSysdate("yyyyMMdd");
      }
    }
    catch (Exception getDateExeception) {
      System.out.println("[jFileReceiver] Problemas ao se obter a data atual para gerar um novo log.");
    }
    // tentando MAX_TRY vezes
    while (! sucess) {
      try {
        // logando a mensagem
        logGenerator.log(_message, _logLevel);
        sucess = true;
      }
      catch (Exception ex) {
        if (amountTry == MAX_TRY) {
          System.out.println("[jFileReceiver] Erro " + ex.toString() + " na tentiva de logar a mensagem abaixo.");
          System.out.println(_message);
          // sleep para caso o recurso esteja desativado, seja re-ativado
          try {
            Thread.sleep(100000);
          }
          catch (Exception threadException) {
            System.out.println("[jFileReceiver.log.LogMessages] Problemas no sleep da thread de log. " + threadException.toString());
          }
        }
      }
      amountTry++;
    }
  }

  /**
   * Método que realiza o processo de logar o stackTrace de uma exceção através
   * da classe LogGenerator mas com o try catch "embutido". No caso de problemas
   * com o LogGenerator, o método loga a stackTrace na saida padrão (System.out.println()).
   *
   * @author Gordo&#153;
   * @param  _exception exceção a ser logada
   * @param  _logLevel nível do log (ATTENTION, DEBUG, STATUS, etc)
   */
  public void logStackTrace (Exception _exception, int _logLevel) {
    try {
      logGenerator.logStackTrace(_exception, _logLevel);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}