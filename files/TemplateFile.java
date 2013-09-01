package jfilereceiver.files;

/**
 * Title:        TemplateFile
 * Description:  Esta classe n�o implementa nada, s� � um template dos plug-ins
 *               do jFileReceiver.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Lenadro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class TemplateFile extends jfilereceiver.files.GeneralFile {

  public void run () {
    // setando o log para o processamento deste tipo de arquivo
    this.setLogProcess();
    logProcess.log("[jFileReceiver.files.UpFile] Startada uma thread para processamento " + this.getFilePattern() + ".", logProcess.logGenerator.STATUS);
    // vari�vel que indica se um arquivo � irregular
    irregular = false;
    // enquanto a Thread n�o for setada para parar
    while (! stop) {
      // se a thread estiver ativa
      if (this.active) {

        /***********************************************************************
         * Implemente aqui o processamento do seu arquivo. Vari�veis �teis e dis-
         * pon�veis.
         *
         * file2process => classe File instanciada com o arquivo a ser processado
         * connectionPool => instancia de connectionPool
         * validFile => cont�m informa��es contidas na configura��o do jFileReceiver
         * logApplication => log da aplica��o
         * logProcess => log do processamento deste arquivo
         */

        // finaliza o processamento deste arquivo
        this.endProcess(true, false);
        // seta o hor�rio do �ltimo acesso a esta thread
        this.lastAccess.setTime(System.currentTimeMillis());
        // pausa a thread para que outro processo possa us�-lo
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