package jfilereceiver.conf;

import java.util.LinkedList;
import util.xmlparser.*;

/**
 * Title:        FileVersion
 * Description:  Armazena as informações sobre uma versão de um tipo de arquivo
 *               válido.
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

  // variáveis
  String codeVersion = new String("");
  XmlVersion xmlVersion;
  TxtVersion txtVersion;

  /**
   * Seta o conteúdo da propriedade codeVersion.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a versão de arquivo em questão
   * @throws NullPointerException se o XPATH for inválido
   */
  public void setCodeVersion (Element _version) throws Exception {
    codeVersion = _version.search(XPATH_CODEVERSION).getElement(0).getValue();
  }

  /**
   * Seta as propriedades de uma versão de arquivo XML ou de um arquivo TXT de acordo
   * com o tipo de arquivo passado por parametro.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a versão de arquivo em questão
   * @throws NullPointerException se o XPATH for inválido
   */
  public void setFileVersion (Element _version) throws Exception {
    this.setCodeVersion(_version);
    if (_version.search(XPATH_XMLVERSION).getSize() > 0)
      setXmlVersion(_version);
    else if (_version.search(XPATH_TXTVERSION).getSize() > 0)
      setTxtVersion(_version);
  }

  /**
   * Seta as propriedades de uma versão de arquivo XML.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a versão de arquivo em questão
   * @throws NullPointerException se o XPATH for inválido
   */
  private void setXmlVersion (Element _version) throws Exception {
    xmlVersion = new XmlVersion();
    // seta txtVersion para null indicando que esta informação se refere a uma
    // versão de arquivo XML
    txtVersion = null;
    // seta as informações sobre a versão
    xmlVersion.setXPathVersion(_version.search(XPATH_INFOVERSION).getElement(0));
    xmlVersion.setFieldsInformation(_version.search(XPATH_INFOVERSION).getElement(0));
  }

  /**
   * Seta as propriedades de uma versão de arquivo TXT.
   *
   * @author Gordo&#153;
   * @param  _version elemento XML que representa a versão de arquivo em questão
   * @throws NullPointerException se o XPATH for inválido
   */
  private void setTxtVersion (Element _version) throws Exception {
    txtVersion = new TxtVersion();
    // seta xmlVersion para null indicando que esta informação se refere a uma
    // versão de arquivo TXT
    xmlVersion = null;
    // seta as informações sobre a versão
    txtVersion.setLineSize(_version.search(XPATH_INFOVERSION).getElement(0));
    txtVersion.setFieldsInformation(_version.search(XPATH_INFOVERSION).getElement(0));
  }

  /**
   * @author Gordo&#153;
   * @return o conteúdo da propriedade codeVersion
   */
  public String getCodeVersion () {
    return codeVersion;
  }

  /**
   * @author Gordo&#153;
   * @return as informações sobre a versão de um arquivo tipo XML
   */
  public XmlVersion getXmlVersion () {
    return xmlVersion;
  }

  /**
   * @author Gordo&#153;
   * @return as informações sobre a versão de um arquivo tipo TXT
   */
  public TxtVersion getTxtVersion () {
    return txtVersion;
  }

}