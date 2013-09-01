package jfilereceiver.files;

import jfilereceiver.log.LogMessages;
import jfilereceiver.conf.ValidFile;
import util.ConnectionPool;
import java.io.File;
import java.util.Date;

/**
 * Title:        FileInterface
 * Description:  Interface para todos os tipos de arquivos que ser�o tratados pelo
 *               jFileReceiver. Todas as classes que forem desenvolvidas para processar
 *               um determinado tipo de arquivo pelo jFileReceiver deve implementar
 *               esta classe e ser "extends" da classe java.lang.Thread
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimar�es (Gordo&#153;)
 * @version      1.0
 */

public interface FileInterface {

  /**
   * M�todo respons�vel por iniciar uma thread. Basicamente � para fazer um
   * this.start na classe para que o m�todo run() possa ser "acionado".
   *
   * @author Gordo&#153;
   */
  public void startThread();

  /**
   * M�todo respons�vel por finalizar uma thread. Seta a propriedade que para o
   * loop principal do m�todo run().
   *
   * @author Gordo&#153;
   */
  public void stopThread();

  /**
   * M�todo respons�vel por pausar ou despausar uma thread. Seta a propriedade
   * que indica se a thread est� ativa ou n�o de acordo com o valor de _pause.
   *
   * @author Gordo&#153;
   * @param  _pause true caso deseje pausar a thread, false caso contr�rio
   */
  public void pauseThread(boolean _pause);

  /**
   * Seta a propriedade que indica qual � o n�vel de log para o processamento de
   * arquivos.
   *
   * @author Gordo&#153;
   * @param  _logLevel n�vel de log para o processamento
   */
  public void setLogLevel(int _logLevel);

  /**
   * Instancia a vari�vel da classe referente ao ConnectionPool com a instancia
   * recebida por par�metro. Faz-se isso para garantir que toda a aplica��o
   * utilizar� apenas um �nico connection pool.
   *
   * @author Gordo&#153;
   * @param  _connectionPool instancia da classe ConnectionPool a ser utilizada
   */
  public void setConnectionPool(ConnectionPool _connectionPool);

  /**
   * Instancia a vari�vel da classe respons�vel por gerar os logs. Faz-se isso
   * para garantir que toda a aplica��o utilize a mesma instancia para gera��o
   * de log.
   *
   * @author Gordo&#153;
   * @param  _logMessages instancia da classe de log
   */
  public void setLogGenerator(LogMessages _logMessages);

  /**
   * M�todo respons�vel por setar para a classe de processamento qual � o arquivo
   * que deve ser processado.
   *
   * @author Gordo&#153;
   * @param  _file inst�ncia da classe File que representa o arquivo a ser processado
   */
  public void setFile(File _file);

  /**
   * Seta a propriedade da classe que cont�m as informa��es sobre o tipo de
   * arquivo a ser processado.
   *
   * @author Gordo&#153;
   * @param  _validFile informa��es sobre o tipo de arquivo a ser procesado
   */
  public void setValidFile(ValidFile _validFile);

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos processados
   * com sucesso ser�o movidos.
   *
   * @author Gordo&#153;
   * @param  _processedDir arquivo de destino dos arquivos processados com sucesso
   */
  public void setProcessedDir(String _processedDir);

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos que apresentarem
   * algum problema durante o seu processamento.
   *
   * @author Gordo&#153;
   * @param  _processedDir arquivo de destino dos arquivos processados com erro(s)
   */
  public void setIrregularDir(String _irregularDir);

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos rejeitados
   * devem ser colocados.
   *
   * @author Gordo&#153;
   * @param  _rejectedDir arquivo de destino dos arquivos rejeitados
   */
  public void setRejectedDir(String _rejectedDir);

  /**
   * Seta a propriedade que indica qual � o tempo m�ximo (em segundos) que uma
   * thread pode ficar ociosa. Extrapolado este tempo e respeitando outras regras,
   * a thread ser� eliminada.
   *
   * @author Gordo&#153;
   * @param  _idleTime tempo maximo (em segundos) para um thread ficar ociosa
   */
  public void setIdleTime(int _idleTime);

  /**
   * Seta a propriedade que indica qual � o tempo (em milissegundos) para
   * o sleep de uma thread.
   *
   * @author Gordo&#153;
   * @param  _sleepTime tempo (em milissegundos) para o sleep de uma thread.
   */
  public void setSleepTime(int _sleepTime);

  /**
   * Seta a propriedade que indica qual � a url do servlet respons�vel por gerar
   * um novo id.
   *
   * @author Gordo&#153;
   * @param  _url url do servlet respons�vel por gerar um novo id.
   */
  public void setNewIdUrl(String _url);

  /**
   * Seta a propriedade da classe que indica qual � o tipo de arquivo processado
   * pela classe.
   *
   * @author Gordo&#153;
   * @param  _pattern o tipo de arquivo que a classe processa
   */
  public void setFilePattern(String _pattern);

  /**
   * Retorna qual tipo de arquivo � processado pela classe.
   *
   * @author Gordo&#153;
   * @return o tipo de arquivo que a classe processa
   */
  public String getFilePattern();

  /**
   * Retorna o conte�do do m�todo Thread.isAlive().
   *
   * @author Gordo&#153;
   * @return m�todo Thread.isAlive()
   */
  public boolean isThreadAlive();

  /**
   * M�todo que retorna o valor da propriedade da classe que indica se o processamento
   * est� ativo ou n�o. Diretamente influenciada pelo m�todo setPause().
   *
   * @author Gordo&#153;
   * @return true se o processamento estiver em andamento, false caso contr�rio
   * @see    #pauseThread(boolean _pause)
   */
  public boolean isActive();

  /**
   * M�todo que retorna o conte�do da vari�vel que indica quando foi finalizado
   * o �ltimo processo pela inst�ncia. � utilizado para monitorar as threas que
   * est�o criadas por�m n�o s�o utilizadas.
   *
   * @author Gordo&#153;
   * @return Date que indica quando foi realizado o �ltimo processamento pela classe
   */
  public java.util.Date getLastAccess();

}