package jfilereceiver.conf;

/**
 * Title:        XmlFieldInformation
 * Description:  Representa as informações de um elemento de um arquivo XML
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class XmlFieldInformation extends FieldInformation {

  // variáveis
  String xPathElement;

  /**
   * Seta o conteúdo da propriedade xPathElement.
   *
   * @author Gordo&#153;
   * @param  _xPathElement xPath do elemento do arquivo XML que representa uma databaseColumn
   * @see FieldInformation
   */
  public void setXPathElement (String _xPathElement) {
    xPathElement = _xPathElement.toUpperCase().trim();
  }

  /**
   * Retorna o conteúdo da propriedade xPathElement.
   *
   * @author Gordo&#153;
   * @return conteúdo da propriedade xPathElement
   */
  public String getXPathElement () {
    return xPathElement;
  }

}