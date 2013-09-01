package jfilereceiver.conf;

/**
 * Title:        TxtFieldInformation
 * Description:  Representa as informações de um trecho de uma linha de um arquivo TXT
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153)
 * @version      1.0
 */

public class TxtFieldInformation extends FieldInformation {

  // variáveis
  int initialPosition;
  int finalPosition;

  /**
   * Seta o conteúdo da propriedade initialPosition.
   *
   * @author Gordo&#153;
   * @param  _initialPosition posição inicial do trecho da String que representa uma databaseColumn
   * @see FieldInformation
   */
  public void setInitialPosition (int _initialPosition) {
    initialPosition = _initialPosition;
  }

  /**
   * Seta o conteúdo da propriedade finalPosition.
   *
   * @author Gordo&#153;
   * @param  _finalPosition posição final do trecho da String que representa uma databaseColumn
   * @see FieldInformation
   */
  public void setFinalPosition (int _finalPosition) {
    finalPosition = _finalPosition;
  }

  /**
   * Retorna o conteúdo da propriedade initialPosition.
   *
   * @author Gordo&#153;
   * @return conteúdo da propriedade initialPosition
   */
  public int getInitialPosition () {
    return initialPosition;
  }

  /**
   * Retorna o conteúdo da propriedade finalPosition.
   *
   * @author Gordo&#153;
   * @return conteúdo da propriedade finalPosition
   */
  public int getFinalPosition () {
    return finalPosition;
  }

}