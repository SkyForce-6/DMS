package org.skyforce.demon.player;

public enum DemonType {
    // Regular Demons
    NEZUKO("Nezuko Kamado"),
    TAMAYO("Tamayo"),
    YUSHIRO("Yushiro"),
    SUSAMARU("Susamaru"),
    MUZAN("Muzan Kibutsuji"),
    YAHABA("Yahaba"),
    SHIZU("Shizu Shinazugawa"),
    TEMPLE_DEMON("Temple Demon"),
    HAND_DEMON("Hand Demon"),
    SWAMP_DEMON("Swamp Demon"),
    ASAKUSA_DEMON("Asakusa Demon"),
    TONGUE_DEMON("Tongue Demon"),
    HORNED_DEMON("Horned Demon"),
    SPIDER_MOTHER("Spider Demon (Mother)"),
    SPIDER_FATHER("Spider Demon (Father)"),
    SPIDER_SON("Spider Demon (Son)"),
    SPIDER_DAUGHTER("Spider Demon (Daughter)"),
    SLASHER("Slasher"),
    MANTIS_DEMON("Mantis Demon"),
    WOODLAND_DEMON("Woodland Demon"),

    // Upper Ranks
    KOKUSHIBO("Upper Rank 1: Kokushibo"),
    DOMA("Upper Rank 2: Doma"),
    AKAZA("Upper Rank 3: Akaza"),
    NAKIME("Upper Rank 4: Nakime"),
    HANTENGU("Former Upper Rank 4: Hantengu"),
    GYOKKO("Upper Rank 5: Gyokko"),
    GYUTARO("Upper Rank 6: Gyutaro"),
    DAKI("Upper Rank 6: Daki"),

    // Lower Ranks
    ENMU("Lower Rank 1: Enmu"),
    ROKURO("Lower Rank 2: Rokuro"),
    WAKURABA("Lower Rank 3: Wakuraba"),
    MUKAGO("Lower Rank 4: Mukago"),
    RUI("Lower Rank 5: Rui"),
    KAMANUE("Lower Rank 6: Kamanue"),
    KYOGAI("Former Lower Rank 6: Kyogai");

    private final String displayName;

    DemonType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DemonType fromString(String name) {
        try {
            return valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}