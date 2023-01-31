package it.gov.pagopa.group.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Random;

public class CFGenerator {

    public static ArrayList<String> CFGeneratorList() {
        final String TOWN_CODE = "X000";
        ArrayList<String> CFList = new ArrayList<>();
        Random rand = new Random();
        String availableLetters = "ABCDEHLMPRST";

        for (int i = 0; i < 5; i++) {
            String cf = "";
            //nameSurname
            cf+= RandomStringUtils.randomAlphabetic(6);
            //year
            cf+= String.valueOf(rand.nextInt(90) + 10);
            //month
            cf+= String.valueOf(availableLetters.charAt(rand.nextInt(availableLetters.length())));
            //day
            cf+= String.valueOf(rand.nextInt(90) + 10);
            //town
            cf+=TOWN_CODE;
            //cin
            cf+= RandomStringUtils.randomAlphabetic(1);
            CFList.add(cf.toUpperCase());
        }
        return CFList;
    }
}
