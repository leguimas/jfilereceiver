package jfilereceiver.conf;

import java.util.LinkedList;
import util.xmlparser.*;

/**
 * Title:        ValidFile
 * Description:  Guarda as informações de um tipo de arquivo válido.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public class ValidFile {

  // constantes
  static final String XPATH_FILEPATT  = "jfrvf-filePattern";
  static final String XPATH_FILETYPE  = "jfrvf-type";
  static final String XPATH_DESCRIPT  = "jfrvf-description";
  static final String XPATH_CLASSNAME = "jfrvf-className";
  static final String XPATH_PROCLOG   = "jfrvf-processLog";
  static final String XPATH_VERSION   = "jfrvf-versions/jfrvf-version";
  static final String XPATH_MAXTHREAD = "jfrvf-maxThreads";
  static final String XPATH_MINTHREAD = "jfrvf-minThreads";
  static final String XPATH_PROCDIR   = "jfrvf-processedOutBox";
  static final String XPATH_IRREGDIR  = "jfrvf-irregularOutBox";
  static final String XPATH_OUTBOXDIR = "jfrob-outBox";
  static final String XPATH_DAILYDIR  = "jfrob-dailyDirectory";
  static final String XPATH_OVERWRITE = "jfrvf-overwriteFiles";
  static final String XPATH_UNICOUT   = "jfrvf-unicOutputFile";
  static final String XPATH_OUTFILE   = "jfruo-fileName";
  static final String XPATH_OUTDAILY  = "jfruo-dailyFile";
  static final String XPATH_EXTRACT   = "jfrvf-extractTo";

  // variáveis
  String filePattern = new String("");
  String fileType = new String("");
  String fileDescription = new String("");
  String fileClassName = new String("");
  String fileProcessLog = new String("");
  String unicOutputFile = new String("");
  String extractTo = new String("");
  LinkedList fileVersions = new LinkedList();
  OutBox processedHome = new OutBox();
  OutBox irregularHome = new OutBox();
  int maxThreads;
  int minThreads;
  boolean overwrite = false;
  boolean dailyFile = false;

  /**
   * Seta o conteúdo da propriedade filePattern. filePattern indica quais são as
   * máscaras válidas para um determinado tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa umum tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setFilePattern (Element _validFile) throws Exception {
    filePattern = _validFile.search(XPATH_FILEPATT).getElement(0).getValue();
  }

  /**
   * Seta o conteúdo da propriedade fileDescription.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa umum tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setFileDescription (Element _validFile) throws Exception {
    fileDescription = _validFile.search(XPATH_DESCRIPT).getElement(0).getValue();
  }

  /**
   * Seta o conteúdo da propriedade fileClassName. Esta propriedade contem o nome
   * da classe que é responsável pelo processamento do tipo de arquivo em questão.
   * Quando se é encontrado um novo arquivo com este tipo, gera-se uma Thread de
   * fileClassName para realizar o processamento do mesmo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa umum tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setFileClassName (Element _validFile) throws Exception {
    fileClassName =  _validFile.search(XPATH_CLASSNAME).getElement(0).getValue();
  }

  /**
   * Seta o conteúdo da propriedade fileProcessLog. fileProcessLog contém o path
   * do arquivo de log de processamento deste tipo de arquivo
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setFileProcessLog (Element _validFile) throws Exception {
    fileProcessLog = _validFile.search(XPATH_PROCLOG).getElement(0).getValue();
  }

  /**
   * Seta o conteúdo da propriedade maxThreads. maxThreads contém o número máximo
   * de threads que devem ser utilizadas para monitorar este tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setMaxThreads (Element _validFile) throws Exception {
    maxThreads = _validFile.search(XPATH_MAXTHREAD).getElement(0).getValueInt().intValue();
  }

  /**
   * Seta o conteúdo da propriedade minThreads. minThreads contém o número máximo
   * de threads que devem ser utilizadas para monitorar este tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setMinThreads (Element _validFile) throws Exception {
    minThreads = _validFile.search(XPATH_MINTHREAD).getElement(0).getValueInt().intValue();
  }

  /**
   * Método responsável por carregar as informações sobre o outbox de arquivos
   * processados.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setProcessedHome (Element _validFile) throws Exception {
    processedHome.setOutBoxDir(_validFile.search(XPATH_PROCDIR + "/" + XPATH_OUTBOXDIR).getElement(0).getValue());
    processedHome.setDailyDir(_validFile.search(XPATH_PROCDIR + "/" + XPATH_DAILYDIR).getElement(0).getValueBoolean().booleanValue());
  }

  /**
   * Método responsável por carregar as informações sobre o outbox de arquivos
   * irregulares.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setIrregularHome (Element _validFile) throws Exception {
    irregularHome.setOutBoxDir(_validFile.search(XPATH_IRREGDIR + "/" + XPATH_OUTBOXDIR).getElement(0).getValue());
    irregularHome.setDailyDir(_validFile.search(XPATH_IRREGDIR + "/" + XPATH_DAILYDIR).getElement(0).getValueBoolean().booleanValue());
  }

  /**
   * Metódo que carrega do arquivo de configurações se os arquivos devem ser
   * sobrescritos ou não.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setOverwriteFiles (Element _validFile) throws Exception {
    overwrite = _validFile.search(XPATH_OVERWRITE).getElement(0).getValueBoolean().booleanValue();
  }

  /**
   * Metódo que carrega o arquivo de saida único para o processamento dos arquivos.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setUnicOutputFile (Element _validFile) throws Exception {
    ElementList elementList = _validFile.search(XPATH_UNICOUT);
    if (elementList.getSize() > 0) {
      unicOutputFile = elementList.getElement(0).getFirstElement(XPATH_OUTFILE).getValue();
      dailyFile = elementList.getElement(0).getFirstElement(XPATH_OUTDAILY).getValueBoolean().booleanValue();
    }
  }

  /**
   * Seta o conteúdo da propriedade extractTo. extractTo contém um diretorio a ser
   * colocado arquivos gerados a partir do processamento de algum outro arquivo
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setExtractTo (Element _validFile) throws Exception {
    ElementList elementList = _validFile.search(XPATH_EXTRACT);
    if (elementList.getSize() > 0) {
      extractTo = elementList.getElement(0).getValue();
    }
  }

  /**
   * Método responsável por carregar todos as informações sobre as versões catalogadas
   * para este tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo válido
   * @throws NullPointerException se o xPath for inválido
   */
  public void setFileVersions (Element _validFile) throws Exception {
    ElementList versions = _validFile.search(XPATH_VERSION);
    FileVersion version;
    fileVersions.clear();
    for (int index = 0; index < versions.getSize(); index ++)
    {
      version = new FileVersion();
      version.setCodeVersion(versions.getElement(index));
      version.setFileVersion(versions.getElement(index));
      fileVersions.add(version);
    }
  }

  /**
   * @author Gordo&#153;
   * @return conteúdo da propriedade filePattern
   */
  public String getFilePattern ()  {
    return filePattern;
  }

  /**
   * @author Gordo&#153;
   * @return conteúdo da propriedade fileDescription
   */
  public String getFileDescription ()  {
    return fileDescription;
  }

  /**
   * @author Gordo&#153;
   * @return conteúdo da propriedade fileClassName
   */
  public String getFileClassName ()  {
    return fileClassName;
  }

  /**
   * @author Gordo&#153;
   * @return conteúdo da propriedade fileProcessLog
   */
  public String getFileProcessLog () {
    return fileProcessLog;
  }

  /**
   * @author Gordo&#153;
   * @return uma instância da classe FileVersion que contém as informações de uma versão de arquivo
   * @see FileVersion
   */
  public FileVersion getFileVersions (int _index) throws Exception {
    return (FileVersion) fileVersions.get(_index);
  }

  /**
   * @author Gordo&#153;
   * @return a quantidade de versões para aquele tipo de arquivo válido
   */
  public int getFileVersionsSize ()  {
    return fileVersions.size();
  }

  /**
   * @author Gordo&#153;
   * @return a quantidade máxima de threads utilizadas para processar este tipo de arquivo
   */
  public int getMaxThreads ()  {
    return maxThreads;
  }

  /**
   * @author Gordo&#153;
   * @return a quantidade mínima de threads utilizadas para processar este tipo de arquivo
   */
  public int getMinThreads ()  {
    return minThreads;
  }

  /**
   * @author Gordo&#153;
   * @return o diretorio que sera utilizado como outbox de arquivos processados
   */
  public String getProcessedHomeOutBoxDir () throws Exception {
    return processedHome.getOutBoxDir();
  }

  /**
   * @author Gordo&#153;
   * @return se haverão subdiretorios para o diretorio de arquivos processados
   */
  public boolean getProcessedHomeDailyDir () {
    return processedHome.getDailyDir();
  }

  /**
   * @author Gordo&#153;
   * @return o diretorio principal dos arquivos processados
   */
  public String getProcessedHomeOutBoxNoDate () {
    return processedHome.getOutBoxDirNoDate();
  }

  /**
   * @author Gordo&#153;
   * @return o diretorio que sera utilizado como outbox de arquivos irregulares
   */
  public String getIrregularHomeOutBoxDir () throws Exception {
    return irregularHome.getOutBoxDir();
  }

  /**
   * @author Gordo&#153;
   * @return se haverão subdiretorios para o diretorio de arquivos irregulares
   */
  public boolean getIrregularHomeDailyDir () {
    return irregularHome.getDailyDir();
  }

  /**
   * @author Gordo&#153;
   * @return o diretorio principal dos arquivos irregulares
   */
  public String getIrregularHomeOutBoxDirNoDate () {
    return irregularHome.getOutBoxDirNoDate();
  }

  /**
   * @author Gordo&#153;
   * @return true caso os arquivos devam ser sobrescritos, false caso contrário
   */
  public boolean getOverwriteFiles () {
    return overwrite;
  }

  /**
   * @author Gordo&#153;
   * @return retorna o arquivo a ser utilizado como outputDi
   */
  public String getOutputFile () {
    return unicOutputFile;
  }

  /**
   * @author Gordo&#153;
   * @return true caso deva ser criado um arquivo de saida por dia, false caso contrário
   */
  public boolean getOutputDaily () {
    return dailyFile;
  }

  /**
   * @author Gordo&#153;
   * @return o diretorio para se gerar novos arquivos
   */
  public String getExtractTo () {
    return extractTo;
  }

}