package jfilereceiver.conf;

/**
 * Title:        TxtFieldInformation
 * Description:  Representa as informa��es de um trecho de uma linha de um arquivo TXT
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153)
 * @version      1.0
 */

public class TxtFieldInformation extends FieldInformation {

  // vari�veis
  int initialPosition;
  int finalPosition;

  /**
   * Seta o conte�do da propriedade initialPosition.
   *
   * @author Gordo&#153;
   * @param  _initialPosition posi��o inicial do trecho da String que representa uma databaseColumn
   * @see FieldInformation
   */
  public void setInitialPosition (int _initialPosition) {
    initialPosition = _initialPosition;
  }

  /**
   * Seta o conte�do da propriedade finalPosition.
   *
   * @author Gordo&#153;
   * @param  _finalPosition posi��o final do trecho da String que representa uma databaseColumn
   * @see FieldInformation
   */
  public void setFinalPosition (int _finalPosition) {
    finalPosition = _finalPosition;
  }

  /**
   * Retorna o conte�do da propriedade initialPosition.
   *
   * @author Gordo&#153;
   * @return conte�do da propriedade initialPosition
   */
  public int getInitialPosition () {
    return initialPosition;
  }

  /**
   * Retorna o conte�do da propriedade finalPosition.
   *
   * @author Gordo&#153;
   * @return conte�do da propriedade finalPosition
   */
  public int getFinalPosition () {
    return finalPosition;
  }

}