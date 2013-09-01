package jfilereceiver.general;

import java.io.File;
import util.FileFilter;
import jfilereceiver.conf.Configuration;

/**
 * Title:        FileTools
 * Description:  Classe que implementa alguns m�todos para facilitar a manipula��o
 *               de arquivos.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public class FileTools {

  // constantes
  public static final String LOCK_INDICATOR = "_";

  /**
   * M�todo que renomeia um arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser renomeado
   * @param  _newFileName novo nome do arquivo
   * @param  _dateTime passando true ser� acrescentado ao nome do arquivo, a data e a hora da opera��o
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   * @throws SecurityException se houver qualquer problema com rela��o a seguran�a / permiss�es
   */
  public File fileRename (File _file, String _newFileName, boolean _dateTime,
                                 boolean _overwrite) throws Exception {
    String newFileName = new String();
    File renamedFile = null;
    int count = 1;
    // realiza sucessivas tentativas de renomear o arquivo
    for (boolean sucess = false; (! sucess); count ++) {
      // monta o nome do arquivo com ou sem a data
      if (_dateTime) {
        // verifica se j� existe uma data no nome do arquivo
        int finalPosition = _newFileName.lastIndexOf(".");
        int initialPosition = _newFileName.substring(0, finalPosition).lastIndexOf(".");
        // verifica se a String delimitada por estes dois valores � uma data
        if ((initialPosition > -1) && (finalPosition > -1)) {
          String auxValue = _newFileName.substring(initialPosition + 1, finalPosition);
          if (DataTools.dateIsValid(auxValue, "yyyyMMddHHmmss")) {
            newFileName = _newFileName.substring(0, initialPosition) +
                          "." +
                          DataTools.getSysdate("yyyyMMddHHmmss") +
                          _newFileName.substring(finalPosition, _newFileName.length());
          }
          else
            newFileName = _newFileName.substring(0, _newFileName.lastIndexOf(".")) +
                          "." +
                          DataTools.getSysdate("yyyyMMddHHmmss") +
                          _newFileName.substring(_newFileName.lastIndexOf("."), _newFileName.length());
        }
        else
          newFileName = _newFileName.substring(0, _newFileName.lastIndexOf(".")) +
                        "." +
                        DataTools.getSysdate("yyyyMMddHHmmss") +
                        _newFileName.substring(_newFileName.lastIndexOf("."), _newFileName.length());
      }
      else
        newFileName = _newFileName;
      // veirifica se j� foi realizada alguma tentativa
      if (count > 1)
        newFileName = newFileName.substring(0, newFileName.lastIndexOf(".")) +
                      "." +
                      new Integer(count).toString() +
                      newFileName.substring(newFileName.lastIndexOf("."), newFileName.length());
      // instancia o novo arquivo
      renamedFile = new File(_file.getParent() + _file.separator + newFileName);
      if (_overwrite) {
        if (_file.renameTo(renamedFile))
          sucess = true;
      }
      else {
        if (! renamedFile.exists()) {
          if (_file.renameTo(renamedFile))
            sucess = true;
        }
      }
    }
    return renamedFile;
  }

  /**
   * M�todo que locka um arquivo. O processo de lock consiste, basicamente, em
   * renomear o arquivo para <LOCK_INDICATOR>arquivo ou arquivo<LOCK_INDICATOR>.
   * Dessa forma, os processos que encontrarem um arquivo com esta sintaxe no nome,
   * automaticamente o ignorar�o.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser lockado
   * @param  _endName indica se o indentificador de lock deve ser colocado no come�o do nome do arquivo (false) ou no fim do nome do arquivo (true)
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   * @throws SecurityException se houver qualquer problema com rela��o a seguran�a / permiss�es
   */
  public File lockFile (File _file, boolean _endName, boolean _overwrite) throws Exception {
    return fileRename (_file, (_endName)?_file.getName() + LOCK_INDICATOR:LOCK_INDICATOR + _file.getName(), false, _overwrite);
  }

  /**
   * M�todo que unlocka um arquivo. O processo de unlock consiste, basicamente, em
   * renomear o arquivo para arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser unlockado
   * @param  _endName indica se o indentificador de lock est� localizado no come�o do nome do arquivo (false) ou no fim do nome do arquivo (true)
   * @param  _dateTime true indica que ao unlockar o arquivo deve se concactenar a data e a hora atual ao nome do arquivo
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   * @throws SecurityException se houver qualquer problema com rela��o a seguran�a / permiss�es
   */
  public File unlockFile (File _file, boolean _endName, boolean _dateTime, boolean _overwrite) throws Exception {
    String newFileName = new String("");
    if (_endName)
      newFileName = _file.getName().substring(_file.getName().length() - LOCK_INDICATOR.length(), _file.getName().length());
    else
      newFileName = _file.getName().substring(LOCK_INDICATOR.length(), _file.getName().length());
    return fileRename (_file, newFileName, _dateTime, _overwrite);
  }

  /**
   * M�todo que move um arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser movido
   * @param  _newDirectory diretorio para onde o arquivo ser� removido
   * @throws SecurityException se houver qualquer problema com rela��o a seguran�a / permiss�es
   */
  public File moveFile (File _file, String _newDirectory) throws Exception {
    String nameFile = new String("");
    // verifica se o �ltimo caracter de _newDirectory j� � um separador para n�o haver caracter duplicado
    if (_file.separator.equals(_newDirectory.substring(_newDirectory.length() - 1, _newDirectory.length())))
      nameFile = _newDirectory + _file.getName();
    else
      nameFile = _newDirectory + _file.separator + _file.getName();
    File movedFile = new File(nameFile);
    // "movendo" o arquivo
    if (! _file.renameTo(movedFile))
      throw new Exception ("[jFileReceiver.general.DataTools] Erro ao renomear o arquivo " + _file.getAbsolutePath() + " para " + _file.getAbsolutePath());
    return movedFile;
  }

  /**
   * M�todo que realiza a c�pia de um arquivo
   *
   * @author Gordo&#153;
   * @param  _fileFrom arquivo que sera copiado
   * @param  _fileTo destino (c�pia)
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   */
  public File copyFile (File _fileFrom, String _fileTo, boolean _overwrite) throws Exception {
    // cria uma inst�ncia de File com o novo nome do arquivo e locka-o
    File fileTo = new File (_fileTo);
    this.lockFile(fileTo, false, _overwrite);
    // realiza a c�pia dos arquivos
    byte[] buffer = new byte[102400];
    java.io.InputStream vlo_Reader = new java.io.FileInputStream(_fileFrom);
    java.io.OutputStream vlo_Writer = new java.io.FileOutputStream(fileTo);
    int vli_lido = vlo_Reader.read(buffer);
    while (vli_lido != -1){
            vlo_Writer.write(buffer,0,vli_lido);
            vli_lido = vlo_Reader.read(buffer);
    }
    vlo_Writer.flush();
    vlo_Writer.close();
    vlo_Reader.close();
    // unlocka o arquivo
    this.unlockFile(fileTo, false, false, _overwrite);
    // retorna a inst�ncia do arquivo copiado
    return fileTo;
  }

  /**
   * M�todo que verifica se um arquivo est� lockado ou n�o. O processo para verifica��o
   * consiste em analisar se no nome do arquivo recebido como par�metro existe no come�o
   * ou no fim o LOCK_INDICATOR.
   *
   * @author Gordo&#153;
   * @return true se existir LOCK_INDICATOR no come�o ou no fim do nome do arquivo. false caso contr�rio
   * @param  _file arquivo que se deseja verificar se est� lockado ou n�o
   */
  public boolean isLocked (File _file) {
    return ((LOCK_INDICATOR.equals(_file.getName().substring(0, LOCK_INDICATOR.length()))) ||
            (LOCK_INDICATOR.equals(_file.getName().substring(_file.getName().length() - LOCK_INDICATOR.length(), _file.getName().length()))));
  }

  /**
   * M�todo que verifica se um arquivo � v�lido para um pattern. Exemplo: verifica
   * se o arquivo jFileReceiver.log � v�lido para o pattern *.log.
   *
   * @author Gordo&#153;
   * @param  _fileName nome do arquivo a ser validado
   * @param  _pattern pattern com a qual o arquivo ser� validado
   */

  public boolean isValid (String _fileName, String _pattern) {
    FileFilter fileFilter = new FileFilter(_pattern);
    return fileFilter.accept(null, _fileName);
  }

  /**
   * M�todo que cria o arquivo que ao ser identificado pelo jFileReceiver paraliza
   * todo o processo.
   *
   * @author Gordo&#153;
   * @param  _dir diret�rio no qual o arquivo deve ser criado
   */
  public void createPauseFile (String _dir) throws Exception {
    // verifica se o diret�rio j� esta com a barra no final
    if (! File.separator.equals(_dir.substring(_dir.length() - 1, _dir.length())))
      _dir = _dir + File.separator;
    // cria-se o novo arquivo
    String pauseFileName = Configuration.CONF_FILE_NAME.substring(0,Configuration.CONF_FILE_NAME.lastIndexOf(".")) + ".pause";
    File pauseFile = new File (_dir + pauseFileName);
    if (! pauseFile.createNewFile())
      throw new Exception ("N�o foi poss�vel criar o arquivo .pause");
  }

}