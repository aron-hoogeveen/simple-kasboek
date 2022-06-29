package ch.bolkhuis.kasboek.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResidentEntityTest {

    @Test
    public void equalsSelf() {
        ResidentEntity residentEntity = new ResidentEntity(
                0,
                "name",
                0,
                0
        );
        assertEquals(residentEntity, residentEntity);
    }

    @Test
    public void equalsOther() {
        ResidentEntity residentEntity1 = new ResidentEntity(
                0,
                "name",
                0,
                0
        );
        ResidentEntity residentEntity2 = new ResidentEntity(
                0,
                "name",
                0,
                0
        );

        assertEquals(residentEntity1, residentEntity2);
    }

    @Test
    public void setPreviousBalanceOnlyAltersPreviousBalance() {
        ResidentEntity residentEntity1 = new ResidentEntity(
                0,
                "name",
                0,
                0
        );
        ResidentEntity residentEntity2 = new ResidentEntity(
                0,
                "name",
                35,
                0
        );

        assertNotEquals(residentEntity1, residentEntity2);
        residentEntity2 = residentEntity2.setPreviousBalance(0);
        assertEquals(residentEntity1, residentEntity2);
    }

}