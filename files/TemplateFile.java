package jfilereceiver.files;

/**
 * Title:        TemplateFile
 * Description:  Esta classe não implementa nada, só é um template dos plug-ins
 *               do jFileReceiver.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Lenadro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class TemplateFile extends jfilereceiver.files.GeneralFile {

  public void run () {
    // setando o log para o processamento deste tipo de arquivo
    this.setLogProcess();
    logProcess.log("[jFileReceiver.files.UpFile] Startada uma thread para processamento " + this.getFilePattern() + ".", logProcess.logGenerator.STATUS);
    // variável que indica se um arquivo é irregular
    irregular = false;
    // enquanto a Thread não for setada para parar
    while (! stop) {
      // se a thread estiver ativa
      if (this.active) {

        /***********************************************************************
         * Implemente aqui o processamento do seu arquivo. Variáveis úteis e dis-
         * poníveis.
         *
         * file2process => classe File instanciada com o arquivo a ser processado
         * connectionPool => instancia de connectionPool
         * validFile => contém informações contidas na configuração do jFileReceiver
         * logApplication => log da aplicação
         * logProcess => log do processamento deste arquivo
         */

        // finaliza o processamento deste arquivo
        this.endProcess(true, false);
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
        logApplication.log("", logApplication.logGenerator.ATTENTION);
        logProcess.log("", logApplication.logGenerator.ATTENTION);
      }
    }
    logProcess.log("", logApplication.logGenerator.STATUS);
  }

}