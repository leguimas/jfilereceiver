package jfilereceiver.conf;

import util.xmlparser.*;
import java.util.LinkedList;

/**
 * Title:        TxtVersion
 * Description:  Representa uma vers�o de um arquivo texto contendo as informa��es
 *               para o processamento do mesmo
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class TxtVersion {

  // constantes
  static final String XPATH_FIELDINFO = "jfrvf-txtVersion/jfrvf-fieldsInformation/jfrtv-txtFieldInformation";
  static final String XPATH_LINESIZE  = "jfrvf-txtVersion/jfrvf-lineSize";
  static final String XPATH_INIPOS    = "jfrtv-initialPosition";
  static final String XPATH_FINPOS    = "jfrtv-finalPosition";
  static final String XPATH_DBCOL     = "jfrtv-databaseColumn";
  static final String XPATH_DATATYPE  = "jfrtv-dataType";

  // vari�veis
  int lineSize;
  LinkedList fieldsInformation = new LinkedList();

  /**
   * Seta o conte�do da propriedade lineSize.
   *
   * @author Gordo&#153;
   * @param  _version elemento do XML que representa uma vers�o de tipo de arquivo
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setLineSize (Element _version) throws Exception {
    lineSize = _version.search(XPATH_LINESIZE).getElement(0).getValueInt().intValue();
  }

  /**
   * Seta as informa��es sobre os trechos de uma linha em um vers�o de arquivo
   * TXT.
   *
   * @author Gordo&#153;
   * @param  _version elemento do XML que representa uma vers�o de tipo de arquivo
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setFieldsInformation (Element _version) throws Exception {
    fieldsInformation.clear();
    TxtFieldInformation txtField;
    // realiza a pesquisa a partir do elemento passado por par�metro
    ElementList confFieldsInformation = _version.search(XPATH_FIELDINFO);
    // percorre os resultados encontrados populando a lista de informa��es
    for (int index = 0; index < confFieldsInformation.getSize(); index ++) {
      txtField = new TxtFieldInformation();
      txtField.setInitialPosition(confFieldsInformation.getElement(index).getFirstElement(XPATH_INIPOS).getValueInt().intValue());
      txtField.setFinalPosition(confFieldsInformation.getElement(index).getFirstElement(XPATH_FINPOS).getValueInt().intValue());
      txtField.setDatabaseColumn(confFieldsInformation.getElement(index).getFirstElement(XPATH_DBCOL).getValue());
      txtField.setDataType(confFieldsInformation.getElement(index).getFirstElement(XPATH_DATATYPE).getValue());
      txtField.setValue("");
      fieldsInformation.add(txtField);
    }
  }

  /**
   * Retorna o a quantidade de campos que � composta uma linha do arquivo TXT de
   * uma vers�o.
   *
   * @author Gordo&#153;
   * @return a quantidade de campos que � composta uma linha do arquivo TXT de uma vers�o
   */
  public int getFieldsInformationSize () {
    return fieldsInformation.size();
  }

  /**
   * Retorna o tamanho de uma linha para uma vers�o de arquivo TXT.
   *
   * @author Gordo&#153;
   * @return tamanho de uma linha para uma vers�o de arquivo TXT
   */
  public int getLineSize () {
    return lineSize;
  }

  /**
   * Retorna as informa��es de um campo de uma linha do arquivo TXT de uma vers�o.
   *
   * @author Gordo&#153;
   * @param  _index �ndice da informa��o que se deseja obter
   * @return as informa��es de um campo de uma linha do arquivo TXT de uma vers�o
   * @throws IndexOutOfBoundsException
   */
  public TxtFieldInformation getTxtFieldInformation (int _index) throws Exception {
    return (TxtFieldInformation) fieldsInformation.get(_index);
  }

}