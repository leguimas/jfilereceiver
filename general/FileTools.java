package jfilereceiver.general;

import java.io.File;
import util.FileFilter;
import jfilereceiver.conf.Configuration;

/**
 * Title:        FileTools
 * Description:  Classe que implementa alguns métodos para facilitar a manipulação
 *               de arquivos.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class FileTools {

  // constantes
  public static final String LOCK_INDICATOR = "_";

  /**
   * Método que renomeia um arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser renomeado
   * @param  _newFileName novo nome do arquivo
   * @param  _dateTime passando true será acrescentado ao nome do arquivo, a data e a hora da operação
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   * @throws SecurityException se houver qualquer problema com relação a segurança / permissões
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
        // verifica se já existe uma data no nome do arquivo
        int finalPosition = _newFileName.lastIndexOf(".");
        int initialPosition = _newFileName.substring(0, finalPosition).lastIndexOf(".");
        // verifica se a String delimitada por estes dois valores é uma data
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
      // veirifica se já foi realizada alguma tentativa
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
   * Método que locka um arquivo. O processo de lock consiste, basicamente, em
   * renomear o arquivo para <LOCK_INDICATOR>arquivo ou arquivo<LOCK_INDICATOR>.
   * Dessa forma, os processos que encontrarem um arquivo com esta sintaxe no nome,
   * automaticamente o ignorarão.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser lockado
   * @param  _endName indica se o indentificador de lock deve ser colocado no começo do nome do arquivo (false) ou no fim do nome do arquivo (true)
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   * @throws SecurityException se houver qualquer problema com relação a segurança / permissões
   */
  public File lockFile (File _file, boolean _endName, boolean _overwrite) throws Exception {
    return fileRename (_file, (_endName)?_file.getName() + LOCK_INDICATOR:LOCK_INDICATOR + _file.getName(), false, _overwrite);
  }

  /**
   * Método que unlocka um arquivo. O processo de unlock consiste, basicamente, em
   * renomear o arquivo para arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser unlockado
   * @param  _endName indica se o indentificador de lock está localizado no começo do nome do arquivo (false) ou no fim do nome do arquivo (true)
   * @param  _dateTime true indica que ao unlockar o arquivo deve se concactenar a data e a hora atual ao nome do arquivo
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   * @throws SecurityException se houver qualquer problema com relação a segurança / permissões
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
   * Método que move um arquivo.
   *
   * @author Gordo&#153;
   * @param  _file arquivo a ser movido
   * @param  _newDirectory diretorio para onde o arquivo será removido
   * @throws SecurityException se houver qualquer problema com relação a segurança / permissões
   */
  public File moveFile (File _file, String _newDirectory) throws Exception {
    String nameFile = new String("");
    // verifica se o último caracter de _newDirectory já é um separador para não haver caracter duplicado
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
   * Método que realiza a cópia de um arquivo
   *
   * @author Gordo&#153;
   * @param  _fileFrom arquivo que sera copiado
   * @param  _fileTo destino (cópia)
   * @param  _overwirte true se o arquivo deve ser sobrescrito, false caso contrario
   */
  public File copyFile (File _fileFrom, String _fileTo, boolean _overwrite) throws Exception {
    // cria uma instância de File com o novo nome do arquivo e locka-o
    File fileTo = new File (_fileTo);
    this.lockFile(fileTo, false, _overwrite);
    // realiza a cópia dos arquivos
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
    // retorna a instância do arquivo copiado
    return fileTo;
  }

  /**
   * Método que verifica se um arquivo está lockado ou não. O processo para verificação
   * consiste em analisar se no nome do arquivo recebido como parâmetro existe no começo
   * ou no fim o LOCK_INDICATOR.
   *
   * @author Gordo&#153;
   * @return true se existir LOCK_INDICATOR no começo ou no fim do nome do arquivo. false caso contrário
   * @param  _file arquivo que se deseja verificar se está lockado ou não
   */
  public boolean isLocked (File _file) {
    return ((LOCK_INDICATOR.equals(_file.getName().substring(0, LOCK_INDICATOR.length()))) ||
            (LOCK_INDICATOR.equals(_file.getName().substring(_file.getName().length() - LOCK_INDICATOR.length(), _file.getName().length()))));
  }

  /**
   * Método que verifica se um arquivo é válido para um pattern. Exemplo: verifica
   * se o arquivo jFileReceiver.log é válido para o pattern *.log.
   *
   * @author Gordo&#153;
   * @param  _fileName nome do arquivo a ser validado
   * @param  _pattern pattern com a qual o arquivo será validado
   */

  public boolean isValid (String _fileName, String _pattern) {
    FileFilter fileFilter = new FileFilter(_pattern);
    return fileFilter.accept(null, _fileName);
  }

  /**
   * Método que cria o arquivo que ao ser identificado pelo jFileReceiver paraliza
   * todo o processo.
   *
   * @author Gordo&#153;
   * @param  _dir diretório no qual o arquivo deve ser criado
   */
  public void createPauseFile (String _dir) throws Exception {
    // verifica se o diretório já esta com a barra no final
    if (! File.separator.equals(_dir.substring(_dir.length() - 1, _dir.length())))
      _dir = _dir + File.separator;
    // cria-se o novo arquivo
    String pauseFileName = Configuration.CONF_FILE_NAME.substring(0,Configuration.CONF_FILE_NAME.lastIndexOf(".")) + ".pause";
    File pauseFile = new File (_dir + pauseFileName);
    if (! pauseFile.createNewFile())
      throw new Exception ("Não foi possível criar o arquivo .pause");
  }

}