package jfilereceiver.conf;

import java.util.LinkedList;
import util.xmlparser.*;

/**
 * Title:        ValidFile
 * Description:  Guarda as informa��es de um tipo de arquivo v�lido.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
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

  // vari�veis
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
   * Seta o conte�do da propriedade filePattern. filePattern indica quais s�o as
   * m�scaras v�lidas para um determinado tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa umum tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setFilePattern (Element _validFile) throws Exception {
    filePattern = _validFile.search(XPATH_FILEPATT).getElement(0).getValue();
  }

  /**
   * Seta o conte�do da propriedade fileDescription.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa umum tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setFileDescription (Element _validFile) throws Exception {
    fileDescription = _validFile.search(XPATH_DESCRIPT).getElement(0).getValue();
  }

  /**
   * Seta o conte�do da propriedade fileClassName. Esta propriedade contem o nome
   * da classe que � respons�vel pelo processamento do tipo de arquivo em quest�o.
   * Quando se � encontrado um novo arquivo com este tipo, gera-se uma Thread de
   * fileClassName para realizar o processamento do mesmo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa umum tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setFileClassName (Element _validFile) throws Exception {
    fileClassName =  _validFile.search(XPATH_CLASSNAME).getElement(0).getValue();
  }

  /**
   * Seta o conte�do da propriedade fileProcessLog. fileProcessLog cont�m o path
   * do arquivo de log de processamento deste tipo de arquivo
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setFileProcessLog (Element _validFile) throws Exception {
    fileProcessLog = _validFile.search(XPATH_PROCLOG).getElement(0).getValue();
  }

  /**
   * Seta o conte�do da propriedade maxThreads. maxThreads cont�m o n�mero m�ximo
   * de threads que devem ser utilizadas para monitorar este tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setMaxThreads (Element _validFile) throws Exception {
    maxThreads = _validFile.search(XPATH_MAXTHREAD).getElement(0).getValueInt().intValue();
  }

  /**
   * Seta o conte�do da propriedade minThreads. minThreads cont�m o n�mero m�ximo
   * de threads que devem ser utilizadas para monitorar este tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setMinThreads (Element _validFile) throws Exception {
    minThreads = _validFile.search(XPATH_MINTHREAD).getElement(0).getValueInt().intValue();
  }

  /**
   * M�todo respons�vel por carregar as informa��es sobre o outbox de arquivos
   * processados.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setProcessedHome (Element _validFile) throws Exception {
    processedHome.setOutBoxDir(_validFile.search(XPATH_PROCDIR + "/" + XPATH_OUTBOXDIR).getElement(0).getValue());
    processedHome.setDailyDir(_validFile.search(XPATH_PROCDIR + "/" + XPATH_DAILYDIR).getElement(0).getValueBoolean().booleanValue());
  }

  /**
   * M�todo respons�vel por carregar as informa��es sobre o outbox de arquivos
   * irregulares.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setIrregularHome (Element _validFile) throws Exception {
    irregularHome.setOutBoxDir(_validFile.search(XPATH_IRREGDIR + "/" + XPATH_OUTBOXDIR).getElement(0).getValue());
    irregularHome.setDailyDir(_validFile.search(XPATH_IRREGDIR + "/" + XPATH_DAILYDIR).getElement(0).getValueBoolean().booleanValue());
  }

  /**
   * Met�do que carrega do arquivo de configura��es se os arquivos devem ser
   * sobrescritos ou n�o.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setOverwriteFiles (Element _validFile) throws Exception {
    overwrite = _validFile.search(XPATH_OVERWRITE).getElement(0).getValueBoolean().booleanValue();
  }

  /**
   * Met�do que carrega o arquivo de saida �nico para o processamento dos arquivos.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setUnicOutputFile (Element _validFile) throws Exception {
    ElementList elementList = _validFile.search(XPATH_UNICOUT);
    if (elementList.getSize() > 0) {
      unicOutputFile = elementList.getElement(0).getFirstElement(XPATH_OUTFILE).getValue();
      dailyFile = elementList.getElement(0).getFirstElement(XPATH_OUTDAILY).getValueBoolean().booleanValue();
    }
  }

  /**
   * Seta o conte�do da propriedade extractTo. extractTo cont�m um diretorio a ser
   * colocado arquivos gerados a partir do processamento de algum outro arquivo
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
   */
  public void setExtractTo (Element _validFile) throws Exception {
    ElementList elementList = _validFile.search(XPATH_EXTRACT);
    if (elementList.getSize() > 0) {
      extractTo = elementList.getElement(0).getValue();
    }
  }

  /**
   * M�todo respons�vel por carregar todos as informa��es sobre as vers�es catalogadas
   * para este tipo de arquivo.
   *
   * @author Gordo&#153;
   * @param  _validFile elemento do XML que representa um tipo de arquivo v�lido
   * @throws NullPointerException se o xPath for inv�lido
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
   * @return conte�do da propriedade filePattern
   */
  public String getFilePattern ()  {
    return filePattern;
  }

  /**
   * @author Gordo&#153;
   * @return conte�do da propriedade fileDescription
   */
  public String getFileDescription ()  {
    return fileDescription;
  }

  /**
   * @author Gordo&#153;
   * @return conte�do da propriedade fileClassName
   */
  public String getFileClassName ()  {
    return fileClassName;
  }

  /**
   * @author Gordo&#153;
   * @return conte�do da propriedade fileProcessLog
   */
  public String getFileProcessLog () {
    return fileProcessLog;
  }

  /**
   * @author Gordo&#153;
   * @return uma inst�ncia da classe FileVersion que cont�m as informa��es de uma vers�o de arquivo
   * @see FileVersion
   */
  public FileVersion getFileVersions (int _index) throws Exception {
    return (FileVersion) fileVersions.get(_index);
  }

  /**
   * @author Gordo&#153;
   * @return a quantidade de vers�es para aquele tipo de arquivo v�lido
   */
  public int getFileVersionsSize ()  {
    return fileVersions.size();
  }

  /**
   * @author Gordo&#153;
   * @return a quantidade m�xima de threads utilizadas para processar este tipo de arquivo
   */
  public int getMaxThreads ()  {
    return maxThreads;
  }

  /**
   * @author Gordo&#153;
   * @return a quantidade m�nima de threads utilizadas para processar este tipo de arquivo
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
   * @return se haver�o subdiretorios para o diretorio de arquivos processados
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
   * @return se haver�o subdiretorios para o diretorio de arquivos irregulares
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
   * @return true caso os arquivos devam ser sobrescritos, false caso contr�rio
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
   * @return true caso deva ser criado um arquivo de saida por dia, false caso contr�rio
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