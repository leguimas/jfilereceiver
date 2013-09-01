package jfilereceiver.files;

import util.xmlparser.*;
import jfilereceiver.files.co.CoInformation;
import java.util.LinkedList;
import java.util.Arrays;
import java.io.*;
import jfilereceiver.general.FileTools;

/**
 * Title:        CoFile
 * Description:  Classe que implementa o processamento de arquivos Cargo-Order.
 *               O processamento, atualmente, deste tipo de arquivo funciona da
 *               seguinte maneira: abre-se o arquivo Cargo-Order, extrai as
 *               informa��es necess�rias e gera-se arquivos UPs com estas informa��es
 *               para que a classe UpFile o processe.
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Gordo#153;
 * @version      1.0
 */

public class CoFile extends GeneralFile {

  private static class ArrayOrdenator implements java.util.Comparator {

    public int compare (Object _obj1, Object _obj2) {
      int vResult = 0;
      CoInformation coInformation1 = new CoInformation();
      CoInformation coInformation2 = new CoInformation();
      coInformation1 = (CoInformation) _obj1;
      coInformation2 = (CoInformation) _obj2;
      return coInformation1.getInfoType().compareTo(coInformation2.getInfoType());
    }

  }

  // constantes
  static final String XPATH_COCODE = "/cargo-order/co-code";
  static final String XPATH_UPLOADSTRIP = "/cargo-order/co-legacyData/upload-strip";
  static final String XPATH_USTYPE = "us-type";
  static final String XPATH_USDATA = "us-data";

  // vari�veis
  XMLDocument xml2Process = null;
  LinkedList coInformations = null;

  /**
   * M�todo que ordena as informa��es pelo tipo de informa��o
   *
   * @author Gordo&#153;
   */
  public void sort () throws Exception {
    ArrayOrdenator ordenator = new ArrayOrdenator();
    CoInformation[] arrayCoInformation = (CoInformation[]) this.coInformations.toArray(new CoInformation[0]);
    java.util.Arrays.sort(arrayCoInformation, ordenator);
    int size = this.coInformations.size();
    this.coInformations.clear();
    for (int indexAux = 0; indexAux < size; indexAux ++)
      this.coInformations.add((CoInformation) arrayCoInformation[indexAux]);
  }

  /**
   * M�todo que monta o nome do arquivo a ser gerado
   *
   * @author Gordo&#153;
   * @param  _infoType tipo do arquivo
   * @param  _coCode c�digo do CO
   */
  public String getFileName (String _infoType, String _coCode) {
    String fileName = validFile.getExtractTo();
    if (!("".equals(fileName)))
      // verifica se o diretorio est� corretamente formatado
      if (!(File.separator.equals(fileName.substring(fileName.length() - 1, fileName.length()))))
        fileName = fileName + File.separator;
    // acrescenta-se o nome do arquivo
    return fileName = fileName + FileTools.LOCK_INDICATOR + _infoType + "-CO-" + _coCode + ".TXT";
  }

  /**
   * M�todo run() desta thread.
   *
   * @author Gordo&#153;
   */
  public void run () {
    // setando o log para o processamento deste tipo de arquivo
    this.setLogProcess();
    logProcess.log("[jFileReceiver.files.CoFile] Startada uma thread para processamento " + this.getFilePattern() + ".", logProcess.logGenerator.STATUS);
    // enquanto a Thread n�o for setada para parar
    while (! stop) {
      // se a thread estiver ativa
      if (this.active) {
        this.irregular = false;
        logProcess.log("[jFileReceiver.files.CoFile] Iniciado o processamento do arquivo " + file2Process.getPath() + ".", logProcess.logGenerator.STATUS);
        // vari�vel para armazenar o problema caso ocorra algum erro na abertura do arquivo XML
        StringBuffer errors = new StringBuffer();
        String coCode;
        // vari�vel que armazena o c�digo do CO
        try {
          // abre o arquivo XML
          xml2Process = new XMLDocument(file2Process.getPath(), true, errors);
          coInformations = new LinkedList();
          // verifica se ocorreu algum problema
          if (!("".equals(errors.toString())))
            throw new Exception (errors.toString());
          // inicia o processamento
          try {
            // obtendo o c�digo do CO
            coCode = xml2Process.search(XPATH_COCODE).getElement(0).getValue();
            // realizando a pesquisa das informa��es necess�rias
            ElementList upStrips = xml2Process.search(XPATH_UPLOADSTRIP);
            CoInformation coInformation;
            // obtendo as informa��es deste XML
            for (int indexSearch = 0; indexSearch < upStrips.getSize(); indexSearch ++) {
              coInformation = new CoInformation();
              coInformation.setInfoType(upStrips.getElement(indexSearch).getFirstElement(XPATH_USTYPE).getValue());
              coInformation.setInfoContents(upStrips.getElement(indexSearch).getFirstElement(XPATH_USDATA).getValue());
              // adiciona a informa��o obtida a lista de informa��es
              coInformations.add(coInformation);
            }
            // verifica se houve alguma informa��o
            if (coInformations.size() > 0) {
              // ordena as informa��es
              this.sort();
              // variavel que controla o nome anterior
              String lastType = new String("");
              String upFileName = new String("");
              // vari�vel que ser� utilizada para escrever-se o novo arquivo
              BufferedWriter outputFile = null;
              coInformation = null;
              // percorrendo todas as linhas da lista
              for (int index = 0; index < coInformations.size(); index ++) {
                try {
                  // obtem a posi��o atual
                  coInformation = (CoInformation) coInformations.get(index);
                  // verifica se o tipo atual � igual ao tipo anterior
                  if (!(lastType.equals(coInformation.getInfoType()))) {
                    // se o outputFile j� estiver instanciado
                    if (outputFile != null) {
                      outputFile.flush();
                      outputFile.close();
                      new FileTools().unlockFile(new File(upFileName), false, false, false);
                    }
                    // instancia com o novo tipo de arquivo
                    upFileName = this.getFileName(coInformation.getInfoType(), coCode);
                    outputFile = new BufferedWriter(new FileWriter(upFileName));
                  }
                  // escreve a linha no arquivo de saida
                  outputFile.write(coInformation.getInfoContents());
                  outputFile.newLine();
                  }
                catch (Exception fileException) {
                  logProcess.log("[jFileReceiver.files.CoFile] Problemas na manipula��o do arquivo " + file2Process.getPath() + ". " + fileException.toString(), logProcess.logGenerator.ATTENTION);
                  logApplication.log("[jFileReceiver.files.CoFile] Problemas na manipula��o do arquivo " + file2Process.getPath() + ". " + fileException.toString(), logApplication.logGenerator.ATTENTION);
                  irregular = true;
                }
                // controla os tipos de informa��o
                if (coInformation != null)
                  lastType = coInformation.getInfoType();
              }
              // fecha o ultimo documento
              if (outputFile != null) {
                outputFile.flush();
                outputFile.close();
                new FileTools().unlockFile(new File(upFileName), false, false, false);
              }
            }
          }
          catch (Exception xmlException) {
            logProcess.log("[jFileReceiver.files.CoFile] Problemas na manipula��o do arquivo " + file2Process.getPath() + ". " + xmlException.toString(), logProcess.logGenerator.ATTENTION);
            logApplication.log("[jFileReceiver.files.CoFile] Problemas na manipula��o do arquivo " + file2Process.getPath() + ". " + xmlException.toString(), logApplication.logGenerator.ATTENTION);
            this.irregular = true;
          }
        }
        catch (Exception xmlFileException) {
          logProcess.log("[jFileReceiver.files.CoFile] Problemas ao abrir o arquivo " + file2Process.getPath() + ". " + xmlFileException.toString(), logProcess.logGenerator.ATTENTION);
          logApplication.log("[jFileReceiver.files.CoFile] Problemas ao abrir o arquivo " + file2Process.getPath() + ". " + xmlFileException.toString(), logApplication.logGenerator.ATTENTION);
          this.irregular = true;
        }
        // desloca o arquivo
        this.endProcess(this.irregular, true);
        // seta o hor�rio do �ltimo acesso a esta thread
        this.lastAccess.setTime(System.currentTimeMillis());
        // pausa a thread para que outro processo possa us�-lo
        this.active = false;
        this.irregular = false;
      }
      // sleep para evitar 100% de CPU
      try {
        Thread.sleep(this.sleepTime);
      }
      catch (Exception sleepException) {
        logApplication.log("[jFileReceiver.files.CoFile] Problemas no sleep da thread UpFile: " + sleepException.toString(), logApplication.logGenerator.ATTENTION);
        logProcess.log("[jFileReceiver.files.CoFile] Problemas no sleep da thread UpFile: " + sleepException.toString(), logApplication.logGenerator.ATTENTION);
      }
    }
    logProcess.log("[jFileReceiver.files.CoFile] Finalizada uma thread para processamento " + this.getFilePattern() + ".", logApplication.logGenerator.STATUS);
  }

}