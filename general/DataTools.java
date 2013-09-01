package jfilereceiver.general;

import java.text.*;
import java.util.*;

/**
 * Title:        DataTools
 * Description:  Alguns métodos úteis para manipulação / formatação de dados
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class DataTools {

  /**
   * Converte um Date para uma String com uma máscara qualquer.
   *
   * @author Gordo&#153;
   * @param  _date Date que deseja se converter
   * @param  _format máscara para formatação de _date
   */
  public static String dateToString ( java.util.Date _date, String _format ) throws Exception {
    String dummy;
    try {
      if (_date == null)
        dummy = "";
      else
        dummy = new String ((new SimpleDateFormat(_format)).format(_date,new StringBuffer(""),new FieldPosition(0)));
    }
    catch (Exception ex) {
      throw ex;
    }
    return dummy;
  }

  /**
   * Converte um Date para uma String com uma máscara qualquer.
   *
   * @author Gordo&#153;
   * @param  _date Date que deseja se converter
   * @param  _format máscara para formatação de _date
   */
  public static String getSysdate (String _format) throws Exception {
    if ("".equals(_format))
      _format = "dd/MM/yyyy HH:mm:ss";
    return dateToString(new Date(System.currentTimeMillis()), _format);
  }

  /**
   * Verifica se uma data é válida.
   *
   * @author Gordo&#153;
   * @param  _date Data a ser validada
   * @param  _format Máscara na qual se encontra a data a ser validada
   */
  public static boolean dateIsValid (String _date, String _format) {
    boolean result = true;
    try {
      java.util.Date vDate;
      // Variáveis para se fazer o parse na String
      SimpleDateFormat vDateFormat = new SimpleDateFormat(_format);
      ParsePosition vParsePosition = new ParsePosition(0);
      GregorianCalendar vGregCalendar = new GregorianCalendar();
      // Algumas configurações
      vGregCalendar.set(GregorianCalendar.YEAR, vGregCalendar.get(GregorianCalendar.YEAR) - 75);
      vDateFormat.set2DigitYearStart(vGregCalendar.getTime());
      vDate = vDateFormat.parse(_date);
    }
    catch (Exception ex) {
      result = false;
    }
    return result;
  }

}