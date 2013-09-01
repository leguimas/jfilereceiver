package jfilereceiver.files.co;

/**
 * Title:        CoInformation
 * Description:  Esta classe representa as informa��es a serem processadas de um
 *               arquivo Cargo-Order.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class CoInformation {

  String infoType = new String("");
  String infoContents = new String("");

  /**
   * M�todo que seta o cont�udo da propriedade infoType. Esta propriedade armazena
   * a qual tipo de arquivo a informa��o se refere.
   *
   * @author Gordo&#153;
   * @param  _infoType o tipo da informa��o
   */
  public void setInfoType (String _infoType) {
    this.infoType = _infoType.trim().toUpperCase();
  }

  /**
   * M�todo que seta o cont�udo da propriedade infoContents. Esta propriedade
   * armazena a informa��o a ser processada.
   *
   * @author Gordo&#153;
   * @param  _infoContents a informa��o
   */
  public void setInfoContents (String _infoContents) {
    this.infoContents = _infoContents;
  }

  /**
   * @author Gordo&#153;
   * @return o tipo da informa��o
   */
  public String getInfoType () {
    return this.infoType;
  }

  /**
   * @author Gordo&#153;
   * @return a informa��o
   */
  public String getInfoContents () {
    return this.infoContents;
  }

}