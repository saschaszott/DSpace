/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class that handle person names.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public final class PersonNameUtil {

    private PersonNameUtil() {

    }

    /**
     * Returns all the variants of the given names.
     *
     * @param  firstName the first name
     * @param  lastName  the last name
     * @param  fullNames the full names
     * @return           all the variants of the given names
     */
    public static Set<String> getAllNameVariants(String firstName, String lastName, List<String> fullNames) {
        Set<String> variants = new HashSet<String>();
        variants.addAll(getNameVariants(firstName, lastName));
        variants.addAll(getNameVariants(fullNames));
        return variants;
    }

    private static List<String> getNameVariants(String firstName, String lastName) {
        List<String> variants = new ArrayList<String>();

        if (StringUtils.isAnyBlank(firstName, lastName)) {
            return variants;
        }

        variants.add(firstName + " " + lastName);
        variants.add(lastName + " " + firstName);

        String[] firstNames = firstName.split(" ");

        if (firstNames.length > 1) {
            variants.addAll(getNameVariants(firstNames, lastName));
        } else {
            String firstNameFirstCharacter = truncateFirstname(firstName);
            variants.add(firstNameFirstCharacter + " " + lastName);
            variants.add(firstNameFirstCharacter + ". " + lastName);
            variants.add(lastName + " " + firstNameFirstCharacter);
            variants.add(lastName + " " + firstNameFirstCharacter + ".");
        }

        return variants;

    }

    private static List<String> getNameVariants(String[] firstNames, String lastName) {
        List<String> variants = new ArrayList<String>();

        for (int i = 0; i < firstNames.length; i++) {
            String truncatedNameOne = truncateFirstname(firstNames[i]);
            variants.add(firstNames[i] + " " + lastName);
            variants.add(lastName + " " + firstNames[i]);
            variants.add(truncatedNameOne + ". " + lastName);
            variants.add(truncatedNameOne + " " + lastName);
            variants.add(lastName + " " + truncatedNameOne + ".");
            variants.add(lastName + " " + truncatedNameOne);
            for (int j = i + 1; j < firstNames.length; j++) {
                String truncatedNameTwo = truncateFirstname(firstNames[j]);
                variants.add(firstNames[i] + " " + firstNames[j] + " " + lastName);
                variants.add(truncatedNameOne + ". " + truncatedNameTwo + ". " + lastName);
                variants.add(truncatedNameOne + " " + truncatedNameTwo + " " + lastName);
                variants.add(lastName + " " + firstNames[i] + " " + firstNames[j]);
                variants.add(lastName + " " + truncatedNameOne + ". " + truncatedNameTwo + ".");
                variants.add(lastName + " " + truncatedNameOne + " " + truncatedNameTwo);
            }
        }

        return variants;
    }

    private static List<String> getNameVariants(List<String> fullNames) {
        return fullNames.stream()
            .filter(Objects::nonNull)
            .map(name -> removeComma(name))
            .distinct()
            .flatMap(name -> getAllNamePermutations(name).stream())
            .distinct()
            .collect(Collectors.toList());
    }

    private static List<String> getAllNamePermutations(String name) {

        List<String> namePermutations = new ArrayList<String>();

        PermutationIterator<String> permutationIterator = new PermutationIterator<String>(List.of(name.split(" ")));

        while (permutationIterator.hasNext()) {
            namePermutations.add(String.join(" ", permutationIterator.next()));
        }

        return namePermutations;
    }

    private static String truncateFirstname(String name) {
        return StringUtils.substring(name, 0, 1);
    }

    private static String removeComma(String name) {
        return StringUtils.normalizeSpace(name.replaceAll(",", " "));
    }

}
