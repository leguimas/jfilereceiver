package jfilereceiver.conf;

import util.xmlparser.*;
import java.util.LinkedList;

/**
 * Title:        XmlVersion
 * Description:  Representa uma versão de um arquivo XML contendo as informações
 *               para o processamento do mesmo
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class XmlVersion {

  // constantes
  static final String XPATH_FIELDINFO    = "jfrvf-xmlVersion/jfrvf-fieldsInformation/jfrxi-xmlFieldInformation";
  static final String XPATH_XPATHVERSION = "jfrvf-xmlVersion/jfrvf-xpathVersion";
  static final String XPATH_XPATHELEMENT = "jfrxi-xpathElement";
  static final String XPATH_DBCOL        = "jfrxi-databaseColumn";
  static final String XPATH_DATATYPE     = "jfrxi-dataType";

  // variáveis
  String xPathVersion;
  LinkedList fieldsInformation = new LinkedList();

  /**
   * Seta o conteúdo da propriedade xPathVersion.
   *
   * @author Gordo&#153;
   * @param  _version elemento que identifica a versão de arquivo XML com a qual se está manipulando
   * @throws NullPointerException se o xPath for inválido
   */
  public void setXPathVersion (Element _version) throws Exception {
    xPathVersion = _version.search(XPATH_XPATHVERSION).getElement(0).getValue();
  }

  /**
   * Seta as informações sobre os elementos de um XML em um versão de arquivo
   * XML.
   *
   * @author Gordo&#153;
   * @param  _version elemento que identifica a versão de arquivo XML com a qual se está manipulando
   * @throws NullPointerException se o xPath for inválido
   */
  public void setFieldsInformation (Element _version) throws Exception {
    fieldsInformation.clear();
    XmlFieldInformation xmlField;
    // realiza a pesquisa a partir do elemento passado por parâmetro
    ElementList confFieldsInformation = _version.search(XPATH_FIELDINFO);
    // percorre os resultados encontrados populando a lista de informações
    for (int index = 0; index < confFieldsInformation.getSize(); index ++) {
      xmlField = new XmlFieldInformation();
      xmlField.setXPathElement(confFieldsInformation.getElement(index).getFirstElement(XPATH_XPATHELEMENT).getValue());
      xmlField.setDatabaseColumn(confFieldsInformation.getElement(index).getFirstElement(XPATH_DBCOL).getValue());
      xmlField.setDataType(confFieldsInformation.getElement(index).getFirstElement(XPATH_DATATYPE).getValue());
      xmlField.setValue("");
      fieldsInformation.add(xmlField);
    }
  }

  /**
   * Retorna o a quantidade de elementos que é composto um arquivo XML de uma versão.
   *
   * @author Gordo&#153;
   * @return a quantidade de elementos que é composto um arquivo XML de uma versão
   */
  public int getXmlFieldsInformationSize() {
    return fieldsInformation.size();
  }

  /**
   * Retorna o xPath que identifica a versão das informações de um arquivo XML.
   *
   * @author Gordo&#153;
   * @return o xPath que identifica a versão das informações de um arquivo XML
   */
  public String getXPathVersion () {
    return xPathVersion;
  }

  /**
   * Retorna as informações de um elemento do arquivo XML de uma versão.
   *
   * @author Gordo&#153;
   * @param  _index índice da informação que se deseja obter
   * @return as informações de um elemento do arquivo XML de uma versão
   * @throws IndexOutOfBoundsException
   */
  public XmlFieldInformation getXmlFieldInformation (int _index) throws Exception {
    return (XmlFieldInformation) fieldsInformation.get(_index);
  }

}