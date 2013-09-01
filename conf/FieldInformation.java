package jfilereceiver.conf;

/**
 * Title:        FieldInformation
 * Description:  Representa as informações genéricas de um campo do arquivo
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class FieldInformation {

  // variáveis
  String databaseColumn;
  String dataType;
  String value;

  /**
   * Seta o conteúdo da propriedade databaseColumn.
   *
   * @author Gordo&#153;
   * @param  _databaseColumn coluna do banco de dados a qual a informação se refere
   */
  public void setDatabaseColumn (String _databaseColumn) {
    databaseColumn = _databaseColumn.toUpperCase().trim();
  }

  /**
   * Seta o conteúdo da propriedade dataType.
   *
   * @author Gordo&#153;
   * @param  _dataType tipo de dados da informação (DATE, STRING, etc)
   */
  public void setDataType (String _dataType) {
    dataType = _dataType.toUpperCase().trim();
  }

  /**
   * Seta o conteúdo da propriedade value.
   *
   * @author Gordo&#153;
   * @param  _value valor da informação
   */
  public void setValue (String _value) {
    value = _value.trim();
  }

  /**
   * Retorna o conteúdo da propriedade databaseColumn.
   *
   * @author Gordo&#153;
   * @return conteúdo da propriedade databaseColumn
   */
  public String getDatabaseColumn () {
    return databaseColumn;
  }

  /**
   * Retorna o conteúdo da propriedade dataType.
   *
   * @author Gordo&#153;
   * @return conteúdo da propriedade dataType
   */
  public String getDataType () {
    return dataType;
  }

  /**
   * Retorna o conteúdo da propriedade value.
   *
   * @author Gordo&#153;
   * @return conteúdo da propriedade value
   */

}