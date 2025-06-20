package com.revitafisio.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

/**
 * Um conversor de atributo JPA para o enum {@link DayOfWeek}.
 *
 * O objetivo desta classe é traduzir o enum DayOfWeek do Java (que representa os dias
 * da semana como MONDAY, TUESDAY, etc.) para um formato que seja mais padronizado
 * e eficiente para armazenar no banco de dados (um número inteiro de 1 a 7).
 *
 * Anotação:
 * @Converter(autoApply = true): Esta é a anotação mais importante.
 * - @Converter: Marca esta classe como um conversor de atributo para o JPA.
 * - autoApply = true: Instrui o JPA a aplicar este conversor automaticamente a **TODOS** os campos
 * do tipo DayOfWeek em **QUALQUER** entidade do projeto. Isso elimina a necessidade de anotar
 * manualmente `@Convert(converter = DayOfWeekConverter.class)` em cada campo, como no
 * {com.revitafisio.entities.agendamentos.HorarioTrabalho#diaDaSemana}, tornando o código
 * das entidades mais limpo e a manutenção mais fácil.
 */
@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {

    /**
     * Converte o enum DayOfWeek (do Java) em um Integer para ser salvo no banco de dados.
     *
     * @param dayOfWeek O enum DayOfWeek da sua entidade (ex: DayOfWeek.MONDAY).
     * @return O valor inteiro correspondente (1 para MONDAY, 2 para TUESDAY, etc.),
     * ou null se o enum de entrada for nulo.
     */
    @Override
    public Integer convertToDatabaseColumn(DayOfWeek dayOfWeek) {
        // Tratamento de erro: se o objeto Java for nulo, salvamos null no banco.
        if (dayOfWeek == null) {
            return null;
        }
        // O metodo getValue() do enum DayOfWeek retorna um inteiro de 1 (segunda) a 7 (domingo),
        // que é um padrão ISO-8601 e ideal para armazenamento.
        return dayOfWeek.getValue();
    }

    /**
     * Converte o valor Integer (vindo do banco de dados) de volta para o enum DayOfWeek.
     *
     * @param value O valor inteiro lido da coluna do banco de dados (ex: 1, 2, ...).
     * @return O enum DayOfWeek correspondente (DayOfWeek.MONDAY, etc.),
     * ou null se o valor do banco for nulo.
     */
    @Override
    public DayOfWeek convertToEntityAttribute(Integer value) {
        // Tratamento de erro: se o valor no banco for nulo, o atributo na entidade Java também será nulo.
        if (value == null) {
            return null;
        }
        // O metodo estático of(int) do enum DayOfWeek faz a conversão inversa,
        // criando o enum correto a partir do valor inteiro.
        return DayOfWeek.of(value);
    }
}
