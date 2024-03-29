package com.chrisgalhur.dice_game.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Manage a game by listing the dice and the result.
 *
 * @version 1.0
 * @author ChrisGalHur
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPlayer {

    //region ATTRIBUTES
    @Id
    @Field(name = "id")
    private String id;
    @Field(name = "Dice 1")
    private int numDice1;
    @Field(name = "Dice 2")
    private int numDice2;
    @Field(name = "result")
    private String result;
    //endregion ATTRIBUTES

    //region CONSTRUCTOR
    /**
     * Constructor of DataPlayerEntity to register a new game.
     *
     * @param numDice1 Result of the first dice.
     * @param numDice2 Result of the second dice.
     * @param result Result of the game.
     */
    public DataPlayer(int numDice1, int numDice2, String result) {
        this.numDice1 = numDice1;
        this.numDice2 = numDice2;
        this.result = result;
    }
    //endregion CONSTRUCTOR
}