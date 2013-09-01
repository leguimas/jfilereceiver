package jfilereceiver.files.co;

/**
 * Title:        CoInformation
 * Description:  Esta classe representa as informações a serem processadas de um
 *               arquivo Cargo-Order.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class CoInformation {

  String infoType = new String("");
  String infoContents = new String("");

  /**
   * Método que seta o contéudo da propriedade infoType. Esta propriedade armazena
   * a qual tipo de arquivo a informação se refere.
   *
   * @author Gordo&#153;
   * @param  _infoType o tipo da informação
   */
  public void setInfoType (String _infoType) {
    this.infoType = _infoType.trim().toUpperCase();
  }

  /**
   * Método que seta o contéudo da propriedade infoContents. Esta propriedade
   * armazena a informação a ser processada.
   *
   * @author Gordo&#153;
   * @param  _infoContents a informação
   */
  public void setInfoContents (String _infoContents) {
    this.infoContents = _infoContents;
  }

  /**
   * @author Gordo&#153;
   * @return o tipo da informação
   */
  public String getInfoType () {
    return this.infoType;
  }

  /**
   * @author Gordo&#153;
   * @return a informação
   */
  public String getInfoContents () {
    return this.infoContents;
  }

}