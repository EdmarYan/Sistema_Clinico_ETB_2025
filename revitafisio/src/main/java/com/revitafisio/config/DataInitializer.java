package com.revitafisio.config;

import com.revitafisio.entities.usuarios.Especialidade;
import com.revitafisio.funcionario.repository.EspecialidadeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente responsável por inicializar dados fundamentais no banco de dados
 * caso eles não existam (ex: ao rodar o projeto em uma nova máquina ou após recriar o BD).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final EspecialidadeRepository especialidadeRepository;

    public DataInitializer(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se existem especialidades cadastradas
        if (especialidadeRepository.count() == 0) {
            logger.info("Nenhuma especialidade encontrada. Inserindo especialidades padrão...");

            List<Especialidade> especialidadesPadrao = List.of(
                    Especialidade.builder().nome("Ortopedia").cor("#4e73df").build(),
                    Especialidade.builder().nome("RPG").cor("#1cc88a").build(),
                    Especialidade.builder().nome("Pilates").cor("#36b9cc").build(),
                    Especialidade.builder().nome("Cardio").cor("#e74a3b").build(),
                    Especialidade.builder().nome("Neurologia").cor("#f6c23e").build()
            );

            especialidadeRepository.saveAll(especialidadesPadrao);
            logger.info("Especialidades padrão cadastradas com sucesso!");
        } else {
            logger.info("Especialidades já estão cadastradas no banco de dados.");
        }
    }
}
