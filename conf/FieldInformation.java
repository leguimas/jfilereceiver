package jfilereceiver.conf;

/**
 * Title:        FieldInformation
 * Description:  Representa as informa��es gen�ricas de um campo do arquivo
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class FieldInformation {

  // vari�veis
  String databaseColumn;
  String dataType;
  String value;

  /**
   * Seta o conte�do da propriedade databaseColumn.
   *
   * @author Gordo&#153;
   * @param  _databaseColumn coluna do banco de dados a qual a informa��o se refere
   */
  public void setDatabaseColumn (String _databaseColumn) {
    databaseColumn = _databaseColumn.toUpperCase().trim();
  }

  /**
   * Seta o conte�do da propriedade dataType.
   *
   * @author Gordo&#153;
   * @param  _dataType tipo de dados da informa��o (DATE, STRING, etc)
   */
  public void setDataType (String _dataType) {
    dataType = _dataType.toUpperCase().trim();
  }

  /**
   * Seta o conte�do da propriedade value.
   *
   * @author Gordo&#153;
   * @param  _value valor da informa��o
   */
  public void setValue (String _value) {
    value = _value.trim();
  }

  /**
   * Retorna o conte�do da propriedade databaseColumn.
   *
   * @author Gordo&#153;
   * @return conte�do da propriedade databaseColumn
   */
  public String getDatabaseColumn () {
    return databaseColumn;
  }

  /**
   * Retorna o conte�do da propriedade dataType.
   *
   * @author Gordo&#153;
   * @return conte�do da propriedade dataType
   */
  public String getDataType () {
    return dataType;
  }

  /**
   * Retorna o conte�do da propriedade value.
   *
   * @author Gordo&#153;
   * @return conte�do da propriedade value
   */

}