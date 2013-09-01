package jfilereceiver.conf;

import util.xmlparser.*;
import java.util.LinkedList;

/**
 * Title:        XmlVersion
 * Description:  Representa uma vers�o de um arquivo XML contendo as informa��es
 *               para o processamento do mesmo
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class XmlVersion {

  // constantes
  static final String XPATH_FIELDINFO    = "jfrvf-xmlVersion/jfrvf-fieldsInformation/jfrxi-xmlFieldInformation";
  static final String XPATH_XPATHVERSION = "jfrvf-xmlVersion/jfrvf-xpathVersion";
  static final String XPATH_XPATHELEMENT = "jfrxi-xpathElement";
  static final String XPATH_DBCOL        = "jfrxi-databaseColumn";
  static final String XPATH_DATATYPE     = "jfrxi-dataType";

  // vari�veis
  String xPathVersion;
  LinkedList fieldsInformation = new LinkedList();

  /**
   * Seta o conte�do da propriedade xPathVersion.
   *
   * @author Gordo&#153;
   * @param  _version elemento que identifica a vers�o de arquivo XML com a qual se est� manipulando
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setXPathVersion (Element _version) throws Exception {
    xPathVersion = _version.search(XPATH_XPATHVERSION).getElement(0).getValue();
  }

  /**
   * Seta as informa��es sobre os elementos de um XML em um vers�o de arquivo
   * XML.
   *
   * @author Gordo&#153;
   * @param  _version elemento que identifica a vers�o de arquivo XML com a qual se est� manipulando
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setFieldsInformation (Element _version) throws Exception {
    fieldsInformation.clear();
    XmlFieldInformation xmlField;
    // realiza a pesquisa a partir do elemento passado por par�metro
    ElementList confFieldsInformation = _version.search(XPATH_FIELDINFO);
    // percorre os resultados encontrados populando a lista de informa��es
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
   * Retorna o a quantidade de elementos que � composto um arquivo XML de uma vers�o.
   *
   * @author Gordo&#153;
   * @return a quantidade de elementos que � composto um arquivo XML de uma vers�o
   */
  public int getXmlFieldsInformationSize() {
    return fieldsInformation.size();
  }

  /**
   * Retorna o xPath que identifica a vers�o das informa��es de um arquivo XML.
   *
   * @author Gordo&#153;
   * @return o xPath que identifica a vers�o das informa��es de um arquivo XML
   */
  public String getXPathVersion () {
    return xPathVersion;
  }

  /**
   * Retorna as informa��es de um elemento do arquivo XML de uma vers�o.
   *
   * @author Gordo&#153;
   * @param  _index �ndice da informa��o que se deseja obter
   * @return as informa��es de um elemento do arquivo XML de uma vers�o
   * @throws IndexOutOfBoundsException
   */
  public XmlFieldInformation getXmlFieldInformation (int _index) throws Exception {
    return (XmlFieldInformation) fieldsInformation.get(_index);
  }

}