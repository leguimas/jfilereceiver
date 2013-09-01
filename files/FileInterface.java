package jfilereceiver.files;

import jfilereceiver.log.LogMessages;
import jfilereceiver.conf.ValidFile;
import util.ConnectionPool;
import java.io.File;
import java.util.Date;

/**
 * Title:        FileInterface
 * Description:  Interface para todos os tipos de arquivos que serão tratados pelo
 *               jFileReceiver. Todas as classes que forem desenvolvidas para processar
 *               um determinado tipo de arquivo pelo jFileReceiver deve implementar
 *               esta classe e ser "extends" da classe java.lang.Thread
 * Copyright:    Copyright (c) 2002
 * Company:      Embralog
 * @author       Leandro Guimarães (Gordo&#153;)
 * @version      1.0
 */

public interface FileInterface {

  /**
   * Método responsável por iniciar uma thread. Basicamente é para fazer um
   * this.start na classe para que o método run() possa ser "acionado".
   *
   * @author Gordo&#153;
   */
  public void startThread();

  /**
   * Método responsável por finalizar uma thread. Seta a propriedade que para o
   * loop principal do método run().
   *
   * @author Gordo&#153;
   */
  public void stopThread();

  /**
   * Método responsável por pausar ou despausar uma thread. Seta a propriedade
   * que indica se a thread está ativa ou não de acordo com o valor de _pause.
   *
   * @author Gordo&#153;
   * @param  _pause true caso deseje pausar a thread, false caso contrário
   */
  public void pauseThread(boolean _pause);

  /**
   * Seta a propriedade que indica qual é o nível de log para o processamento de
   * arquivos.
   *
   * @author Gordo&#153;
   * @param  _logLevel nível de log para o processamento
   */
  public void setLogLevel(int _logLevel);

  /**
   * Instancia a variável da classe referente ao ConnectionPool com a instancia
   * recebida por parâmetro. Faz-se isso para garantir que toda a aplicação
   * utilizará apenas um único connection pool.
   *
   * @author Gordo&#153;
   * @param  _connectionPool instancia da classe ConnectionPool a ser utilizada
   */
  public void setConnectionPool(ConnectionPool _connectionPool);

  /**
   * Instancia a variável da classe responsável por gerar os logs. Faz-se isso
   * para garantir que toda a aplicação utilize a mesma instancia para geração
   * de log.
   *
   * @author Gordo&#153;
   * @param  _logMessages instancia da classe de log
   */
  public void setLogGenerator(LogMessages _logMessages);

  /**
   * Método responsável por setar para a classe de processamento qual é o arquivo
   * que deve ser processado.
   *
   * @author Gordo&#153;
   * @param  _file instância da classe File que representa o arquivo a ser processado
   */
  public void setFile(File _file);

  /**
   * Seta a propriedade da classe que contém as informações sobre o tipo de
   * arquivo a ser processado.
   *
   * @author Gordo&#153;
   * @param  _validFile informações sobre o tipo de arquivo a ser procesado
   */
  public void setValidFile(ValidFile _validFile);

  /**
   * Seta a propriedade que indica para qual diretorio os arquivos processados
   * com sucesso serão movidos.
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
   * Seta a propriedade que indica qual é o tempo máximo (em segundos) que uma
   * thread pode ficar ociosa. Extrapolado este tempo e respeitando outras regras,
   * a thread será eliminada.
   *
   * @author Gordo&#153;
   * @param  _idleTime tempo maximo (em segundos) para um thread ficar ociosa
   */
  public void setIdleTime(int _idleTime);

  /**
   * Seta a propriedade que indica qual é o tempo (em milissegundos) para
   * o sleep de uma thread.
   *
   * @author Gordo&#153;
   * @param  _sleepTime tempo (em milissegundos) para o sleep de uma thread.
   */
  public void setSleepTime(int _sleepTime);

  /**
   * Seta a propriedade que indica qual é a url do servlet responsável por gerar
   * um novo id.
   *
   * @author Gordo&#153;
   * @param  _url url do servlet responsável por gerar um novo id.
   */
  public void setNewIdUrl(String _url);

  /**
   * Seta a propriedade da classe que indica qual é o tipo de arquivo processado
   * pela classe.
   *
   * @author Gordo&#153;
   * @param  _pattern o tipo de arquivo que a classe processa
   */
  public void setFilePattern(String _pattern);

  /**
   * Retorna qual tipo de arquivo é processado pela classe.
   *
   * @author Gordo&#153;
   * @return o tipo de arquivo que a classe processa
   */
  public String getFilePattern();

  /**
   * Retorna o conteúdo do método Thread.isAlive().
   *
   * @author Gordo&#153;
   * @return método Thread.isAlive()
   */
  public boolean isThreadAlive();

  /**
   * Método que retorna o valor da propriedade da classe que indica se o processamento
   * está ativo ou não. Diretamente influenciada pelo método setPause().
   *
   * @author Gordo&#153;
   * @return true se o processamento estiver em andamento, false caso contrário
   * @see    #pauseThread(boolean _pause)
   */
  public boolean isActive();

  /**
   * Método que retorna o conteúdo da variável que indica quando foi finalizado
   * o último processo pela instãncia. É utilizado para monitorar as threas que
   * estão criadas porém não são utilizadas.
   *
   * @author Gordo&#153;
   * @return Date que indica quando foi realizado o último processamento pela classe
   */
  public java.util.Date getLastAccess();

}