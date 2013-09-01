package jfilereceiver.conf;

/**
 * Title:        XmlFieldInformation
 * Description:  Representa as informa��es de um elemento de um arquivo XML
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class XmlFieldInformation extends FieldInformation {

  // vari�veis
  String xPathElement;

  /**
   * Seta o conte�do da propriedade xPathElement.
   *
   * @author Gordo&#153;
   * @param  _xPathElement xPath do elemento do arquivo XML que representa uma databaseColumn
   * @see FieldInformation
   */
  public void setXPathElement (String _xPathElement) {
    xPathElement = _xPathElement.toUpperCase().trim();
  }

  /**
   * Retorna o conte�do da propriedade xPathElement.
   *
   * @author Gordo&#153;
   * @return conte�do da propriedade xPathElement
   */
  public String getXPathElement () {
    return xPathElement;
  }

}