package jfilereceiver.conf;

import java.io.File;
import jfilereceiver.general.DataTools;

/**
 * Title:        OutBox
 * Description:  Guarda as informa��es sobre um outbox: o diret�rio que sera utilizado
 *               e se deve ser criado um diret�rio por dia para o processamento dos ar-
 *               quivos.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class OutBox {

  // vari�veis
  String outBoxDir = new String("");
  boolean dailyDir = true;

  /**
   * Seta a propriedade outBoxDir. Esta propriedade armazena o diret�rio que
   * ser� utilizado como outbox.
   *
   * @author Gordo&#153;
   * @param  _outBoxDir valor a ser atribu�do para outBoxDir
   */
  public void setOutBoxDir (String _outBoxDir) {
    outBoxDir = _outBoxDir;
  }

  /**
   * Seta a propriedade dailyDir. Esta propriedade indica se deve ser criado um
   * diret�rio por dia ao se processar os arquivos.
   *
   * @author Gordo&#153;
   * @param  true caso deseje que se crie diret�rios por dia, false caso contr�rio
   */
  public void setDailyDir (boolean _dailyDir) {
    dailyDir = _dailyDir;
  }

  /**
   * @author Gordo&#153;
   * @return conteudo da propriedade outBoxDir
   */
  public String getOutBoxDir () throws Exception {
    String result = outBoxDir;
    // verifica se existe subdiretorio
    if (this.dailyDir)
      if (new File(this.outBoxDir).separator.equals(this.outBoxDir.substring(this.outBoxDir.length() - 1, this.outBoxDir.length())))
        result = this.outBoxDir + DataTools.getSysdate("yyyyMMdd");
      else
        result = this.outBoxDir + new File(this.outBoxDir).separator + DataTools.getSysdate("yyyyMMdd");
    return result;
  }

  /**
   * @author Gordo&#153;
   * @return conteudo da propriedade dailyDir
   */
  public boolean getDailyDir () {
    return dailyDir;
  }

  /**
   * @author Gordo&#153;
   * @return conte�d da propriedade outBouDir;
   */
  public String getOutBoxDirNoDate () {
    return outBoxDir;
  }

}