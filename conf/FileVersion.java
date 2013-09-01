package jfilereceiver.conf;

import java.util.LinkedList;
import util.xmlparser.*;

/**
 * Title:        FileVersion
 * Description:  Armazena as informa��es sobre uma vers�o de um tipo de arquivo
 *               v�lido.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimaraes (Gordo&#153;)
 * @version      1.0
 */

public class FileVersion {

  // constantes
  static final String XPATH_CODEVERSION = "jfrvf-codeVersion";
  static final String XPATH_INFOVERSION = "jfrvf-infoVersion";
  static final String XPATH_TXTVERSION  = "jfrvf-infoVersion/jfrvf-txtVersion";
  static final String XPATH_XMLVERSION  = "jfrvf-infoVersion/jfrvf-xmlVersion";

  // vari�veis
  String codeVersion = new String("");
  XmlVersion xmlVersion;
  TxtVersion txtVersion;

  /**
   * Seta o conte�do da propriedade codeVersion.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a vers�o de arquivo em quest�o
   * @throws NullPointerException se o XPATH for inv�lido
   */
  public void setCodeVersion (Element _version) throws Exception {
    codeVersion = _version.search(XPATH_CODEVERSION).getElement(0).getValue();
  }

  /**
   * Seta as propriedades de uma vers�o de arquivo XML ou de um arquivo TXT de acordo
   * com o tipo de arquivo passado por parametro.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a vers�o de arquivo em quest�o
   * @throws NullPointerException se o XPATH for inv�lido
   */
  public void setFileVersion (Element _version) throws Exception {
    this.setCodeVersion(_version);
    if (_version.search(XPATH_XMLVERSION).getSize() > 0)
      setXmlVersion(_version);
    else if (_version.search(XPATH_TXTVERSION).getSize() > 0)
      setTxtVersion(_version);
  }

  /**
   * Seta as propriedades de uma vers�o de arquivo XML.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a vers�o de arquivo em quest�o
   * @throws NullPointerException se o XPATH for inv�lido
   */
  private void setXmlVersion (Element _version) throws Exception {
    xmlVersion = new XmlVersion();
    // seta txtVersion para null indicando que esta informa��o se refere a uma
    // vers�o de arquivo XML
    txtVersion = null;
    // seta as informa��es sobre a vers�o
    xmlVersion.setXPathVersion(_version.search(XPATH_INFOVERSION).getElement(0));
    xmlVersion.setFieldsInformation(_version.search(XPATH_INFOVERSION).getElement(0));
  }

  /**
   * Seta as propriedades de uma vers�o de arquivo TXT.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a vers�o de arquivo em quest�o
   * @throws NullPointerException se o XPATH for inv�lido
   */
  private void setTxtVersion (Element _version) throws Exception {
    txtVersion = new TxtVersion();
    // seta xmlVersion para null indicando que esta informa��o se refere a uma
    // vers�o de arquivo TXT
    xmlVersion = null;
    // seta as informa��es sobre a vers�o
    txtVersion.setLineSize(_version.search(XPATH_INFOVERSION).getElement(0));
    txtVersion.setFieldsInformation(_version.search(XPATH_INFOVERSION).getElement(0));
  }

  /**
   * @author Gordo&#153;
   * @return o conte�do da propriedade codeVersion
   */
  public String getCodeVersion () {
    return codeVersion;
  }

  /**
   * @author Gordo&#153;
   * @return as informa��es sobre a vers�o de um arquivo tipo XML
   */
  public XmlVersion getXmlVersion () {
    return xmlVersion;
  }

  /**
   * @author Gordo&#153;
   * @return as informa��es sobre a vers�o de um arquivo tipo TXT
   */
  public TxtVersion getTxtVersion () {
    return txtVersion;
  }

}