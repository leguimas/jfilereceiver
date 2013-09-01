package jfilereceiver.general;

import util.ConnectionPool;
import java.sql.*;
import jfilereceiver.conf.*;
import java.util.LinkedList;

/**
 * Title:        DatabaseTools
 * Description:  Alguns métodos úteis para facilitar o acesso a banco de dados.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Gordo&#153;
 * @version      1.0
 */

public class DatabaseTools {

  // constantes referentes a obtenção de um novo id
  public static final String NEW_ID_UPTYPE = "upfile";

  /**
   * Método que recebe as informações sobre uma versão de arquivo TXT e devolve,
   * em formato SQL, as colunas dessa versão.
   *
   * @author Gordo&#153;
   * @param  _txtVersion a versão de arquivo txt
   * @return as colunas desta versão
   */
  public String getStringColumns (TxtVersion _txtVersion) throws Exception {
    String result = new String("");
    TxtFieldInformation txtField;
    // percorre todos as informações da versão de TXT e monta o resultado
    for (int txtIndex = 0; txtIndex < _txtVersion.getFieldsInformationSize(); txtIndex ++) {
      txtField = _txtVersion.getTxtFieldInformation(txtIndex);
      result = result + txtField.getDatabaseColumn();
      // para evitar seja adicionado "," à última coluna
      if (txtIndex != _txtVersion.getFieldsInformationSize() - 1)
        result = result + ", ";
    }
    return result;
  }

  /**
   * Método que recebe as informações sobre uma versão de arquivo XML e devolve,
   * em formato SQL, as colunas dessa versão.
   *
   * @author Gordo&#153;
   * @param  _xmlVersion a versão de arquivo xml
   * @return as colunas desta versão
   */
  public String getStringColumns (XmlVersion _xmlVersion) throws Exception {
    String result = new String("");
    XmlFieldInformation xmlField;
    // percorre todos as informações da versão de XML e monta o resultado
    for (int xmlIndex = 0; xmlIndex < _xmlVersion.getXmlFieldsInformationSize(); xmlIndex ++) {
      xmlField = _xmlVersion.getXmlFieldInformation(xmlIndex);
      result = result + xmlField.getDatabaseColumn();
      // para evitar seja adicionado "," à última coluna
      if (xmlIndex != _xmlVersion.getXmlFieldsInformationSize() - 1)
        result = result + ", ";
    }
    return result;
  }

  /**
   * Método que recebe as informações sobre uma versão de arquivo TXT e devolve,
   * em formato SQL, os valores a serem inseridos.
   *
   * @author Gordo&#153;
   * @param  _txtVersion a versão de arquivo txt
   * @param  _values valores das informações de _txtVersion
   * @return as colunas desta versão
   */
  public String getStringColumns (TxtVersion _txtVersion, LinkedList _values) throws Exception {
    String result = new String("");
    TxtFieldInformation txtField;
    // percorre todos as informações da versão de TXT e monta o resultado
    for (int txtIndex = 0; txtIndex < _txtVersion.getFieldsInformationSize(); txtIndex ++) {
      String value = new String("");
      String auxValue = new String("");
      txtField = _txtVersion.getTxtFieldInformation(txtIndex);
      // formata o valor de acordo com o tipo do campo
      if ("STRING".equals(txtField.getDataType().toUpperCase()))
        value = "'" + (String) _values.get(txtIndex) + "'";
      else if ("NUMBER".equals(txtField.getDataType().toUpperCase()))
        value = (String) _values.get(txtIndex);
      else if ("BOOLEAN".equals(txtField.getDataType().toUpperCase())) {
        if (("T".equals(_values.get(txtIndex).toString().toUpperCase())) || ("S".equals(_values.get(txtIndex).toString().toUpperCase())))
          value = "true";
        else if (("F".equals(_values.get(txtIndex).toString().toUpperCase())) || ("N".equals(_values.get(txtIndex).toString().toUpperCase())))
          value = "false";
      }
      // formata a data no padrão yyyy-MM-dd
      else if ("DATE".equals(txtField.getDataType().toUpperCase())) {
        auxValue = (String) _values.get(txtIndex);
        try {
          value = auxValue.substring(0,4) + "-" + auxValue.substring(4,6) + "-" + auxValue.substring(6,8);
          // verifica se a data é válida
          if (! DataTools.dateIsValid(value, "yyyy-MM-dd"))
            throw new Exception ("Valor DATE inválido: " + value);
          value = "'" + value + "'";
        }
        catch (Exception formatException) {
          throw new Exception ("Problemas ao se formatar a data " + auxValue + " na máscara yyyy-MM-dd. " + formatException.toString());
        }
      }
      // formata data/horário no padrão yyyy-MM-dd hh:mm:ss
      else if ("DATETIME".equals(txtField.getDataType().toUpperCase())) {
        auxValue = (String) _values.get(txtIndex);
        // verifica se a data/hora está no formato yyyyMMddhhmmss
        for (int indexTime = auxValue.length(); indexTime < 15; indexTime ++)
          // completa com "0" a direita para preencher o tamanho ideal
          auxValue = auxValue + "0";
        try {
          value = auxValue.substring(0,4) + "-" + auxValue.substring(4,6) + "-" + auxValue.substring(6,8) +
                  " " + auxValue.substring(8,10) + ":" + auxValue.substring(10,12) + ":" + auxValue.substring(12,14);
          // verifica se a data é valida
          if (! DataTools.dateIsValid(value, "yyyy-MM-dd HH:mm:ss"))
            throw new Exception ("Valor DATE/TIME inválido: " + value);
          value = "'" + value + "'";
        }
        catch (Exception formatException) {
          throw new Exception ("Problemas ao se formatar o tempo " + auxValue + " na máscara hh:mm:ss. " + formatException.toString());
        }
      }
      // formata o horário no padrão hh:mm:ss
      else if ("TIME".equals(txtField.getDataType().toUpperCase())) {
        auxValue = (String) _values.get(txtIndex);
        // verifica se o tempo está no formato hhmmss
        for (int indexTime = auxValue.length(); indexTime < 7; indexTime ++)
          // completa com "0" a direita para preencher o tamanho ideal
          auxValue = auxValue + "0";
        try {
          value = auxValue.substring(0,2) + ":" + auxValue.substring(2,4) + ":" + auxValue.substring(4,6);
          if (! DataTools.dateIsValid(value, "HH:mm:ss"))
            throw new Exception ("Valor TIME inválido:" + value);
          value = "'" + value + "'";
        }
        catch (Exception formatException) {
          throw new Exception ("Problemas ao se formatar o tempo " + auxValue + " na máscara hh:mm:ss. " + formatException.toString());
        }
      }
      result = result + value;
      // para evitar seja adicionado "," à última coluna
      if (txtIndex != _txtVersion.getFieldsInformationSize() - 1)
        result = result + ", ";
    }
    return result;
  }

  /**
   * Método que recebe as informações sobre uma versão de arquivo XML e devolve,
   * em formato SQL, os valores a serem inseridos.
   *
   * @author Gordo&#153;
   * @param  _xmlVersion a versão de arquivo xml
   * @param  _values valores das informações de _xmlVersion
   * @return as colunas desta versão
   */
  public String getStringColumns (XmlVersion _xmlVersion, LinkedList _values) throws Exception {
    String result = new String("");
    XmlFieldInformation xmlField;
    // percorre todos as informações da versão de XML e monta o resultado
    for (int xmlIndex = 0; xmlIndex < _xmlVersion.getXmlFieldsInformationSize(); xmlIndex ++) {
      String value = new String("");
      xmlField = _xmlVersion.getXmlFieldInformation(xmlIndex);
      // formata o valor de acordo com o tipo do campo
      if ("STRING".equals(xmlField.getDataType().toUpperCase()))
        value = "'" + (String) _values.get(xmlIndex) + "'";
      else if ("NUMBER".equals(xmlField.getDataType().toUpperCase()))
        value = (String) _values.get(xmlIndex);
      else if ("BOOLEAN".equals(xmlField.getDataType().toUpperCase())) {
        if (("T".equals(_values.get(xmlIndex).toString().toUpperCase())) || ("S".equals(_values.get(xmlIndex).toString().toUpperCase())))
          value = "true";
        else if (("F".equals(_values.get(xmlIndex).toString().toUpperCase())) || ("N".equals(_values.get(xmlIndex).toString().toUpperCase())))
          value = "false";
      }
      result = result + value;
      // para evitar seja adicionado "," à última coluna
      if (xmlIndex != _xmlVersion.getXmlFieldsInformationSize() - 1)
        result = result + ", ";
    }
    return result;
  }

}