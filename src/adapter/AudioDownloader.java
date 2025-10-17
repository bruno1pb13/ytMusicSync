package adapter;

import domain.Video;

/**
 * Interface para download de áudio.
 * Abstração permite trocar implementação (yt-dlp, youtube-dl, etc).
 */
public interface AudioDownloader {
    /**
     * Faz download do áudio de um vídeo.
     * @param video Vídeo a ser baixado
     * @param outputDirectory Diretório de destino
     * @return true se download foi bem-sucedido
     */
    boolean download(Video video, String outputDirectory);

    /**
     * Verifica se a ferramenta de download está disponível.
     */
    boolean isAvailable();

    /**
     * Retorna a versão da ferramenta.
     */
    String getVersion();
}
