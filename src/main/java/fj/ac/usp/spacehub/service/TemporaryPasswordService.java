package fj.ac.usp.spacehub.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;


@Service
public class TemporaryPasswordService {


    /* =========================================================
       SECURE RANDOM GENERATOR
       ========================================================= */

    private static final SecureRandom RANDOM =
            new SecureRandom();



    /* =========================================================
       PASSWORD CHARACTER SETS
       ========================================================= */

    private static final String UPPERCASE =
            "ABCDEFGHJKLMNPQRSTUVWXYZ";


    private static final String LOWERCASE =
            "abcdefghijkmnopqrstuvwxyz";


    private static final String NUMBERS =
            "23456789";


    private static final String SPECIAL =
            "@#$%&*!";


    private static final String ALL_CHARACTERS =
            UPPERCASE
                    +
            LOWERCASE
                    +
            NUMBERS
                    +
            SPECIAL;



    /* =========================================================
       TEMPORARY PASSWORD LENGTH
       ========================================================= */

    private static final int PASSWORD_LENGTH =
            12;



    /* =========================================================
       GENERATE TEMPORARY PASSWORD
       ========================================================= */

    public String generate() {


        char[] password =
                new char[PASSWORD_LENGTH];



        /*
         * Guarantee at least:
         *
         * 1 uppercase letter
         * 1 lowercase letter
         * 1 number
         * 1 special character
         */

        password[0] =
                randomCharacter(
                        UPPERCASE
                );


        password[1] =
                randomCharacter(
                        LOWERCASE
                );


        password[2] =
                randomCharacter(
                        NUMBERS
                );


        password[3] =
                randomCharacter(
                        SPECIAL
                );



        /*
         * Fill the remaining characters using the complete
         * character set.
         */

        for (
                int i = 4;
                i < PASSWORD_LENGTH;
                i++
        ) {

            password[i] =
                    randomCharacter(
                            ALL_CHARACTERS
                    );

        }



        /*
         * Shuffle the generated characters.
         *
         * Without this step the first four characters would
         * always follow the same uppercase/lowercase/number/
         * special-character pattern.
         */

        shuffle(
                password
        );


        return new String(
                password
        );
    }



    /* =========================================================
       RANDOM CHARACTER
       ========================================================= */

    private char randomCharacter(
            String characters
    ) {


        int position =
                RANDOM.nextInt(
                        characters.length()
                );


        return characters.charAt(
                position
        );
    }



    /* =========================================================
       SHUFFLE PASSWORD
       ========================================================= */

    private void shuffle(
            char[] password
    ) {


        for (
                int i = password.length - 1;
                i > 0;
                i--
        ) {


            int position =
                    RANDOM.nextInt(
                            i + 1
                    );


            char temporaryCharacter =
                    password[i];


            password[i] =
                    password[position];


            password[position] =
                    temporaryCharacter;

        }

    }

}