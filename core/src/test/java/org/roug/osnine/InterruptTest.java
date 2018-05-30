package org.roug.osnine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InterruptTest {

    private MC6809 myTestCPU;

    @Before
    public void setUp() {
        myTestCPU = new MC6809(0x2000);
        // Set register S to point to 517
        myTestCPU.s.set(517);
        myTestCPU.cc.set(0x0F);
        myTestCPU.write_word(MC6809.IRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
    }


    /**
     * Test simple IRQ pushing all registers to the S stack.
     */
    @Test
    public void raiseSimpleIRQ() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        myTestCPU.a.set(1);
        myTestCPU.b.set(2);
        myTestCPU.dp.set(3);
        myTestCPU.x.set(0x0405);
        myTestCPU.y.set(0x0607);
        myTestCPU.u.set(0x0809);

        myTestCPU.write_word(MC6809.IRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        myTestCPU.getBus().signalIRQ(true);
        myTestCPU.execute();
        assertEquals(0x1234, myTestCPU.pc.intValue());

        myTestCPU.getBus().signalIRQ(false);
        myTestCPU.execute();
        assertEquals(0xB01, myTestCPU.pc.intValue());
        assertEquals(0x8F, myTestCPU.cc.intValue()); // Flag E is set by IRQ
        assertEquals(0x01, myTestCPU.a.intValue());
        assertEquals(0x02, myTestCPU.b.intValue());
        assertEquals(517, myTestCPU.s.intValue());
        assertEquals(0x01, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
        assertEquals(0x09, myTestCPU.read(517 - 3)); // Check that U-low was pushed.
        assertEquals(0x08, myTestCPU.read(517 - 4)); // Check that U-high was pushed.
        assertEquals(0x07, myTestCPU.read(517 - 5)); // Check that Y-low was pushed.
        assertEquals(0x06, myTestCPU.read(517 - 6)); // Check that Y-high was pushed.
        assertEquals(0x05, myTestCPU.read(517 - 7)); // Check that X-low was pushed.
        assertEquals(0x04, myTestCPU.read(517 - 8)); // Check that X-high was pushed.
        assertEquals(0x03, myTestCPU.read(517 - 9)); // Check that DP was pushed.
        assertEquals(0x02, myTestCPU.read(517 - 10)); // Check that B was pushed.
        assertEquals(0x01, myTestCPU.read(517 - 11)); // Check that A was pushed.
        assertEquals(0x8F, myTestCPU.read(517 - 12)); // Check that CC was pushed.
    }

    /**
     * Test Non-maskable IRQ pushing all registers to the S stack.
     */
    @Test
    public void raiseNonmaskableIRQ() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        myTestCPU.a.set(1);
        myTestCPU.b.set(2);
        myTestCPU.dp.set(3);
        myTestCPU.x.set(0x0405);
        myTestCPU.y.set(0x0607);
        myTestCPU.u.set(0x0809);

        myTestCPU.write_word(MC6809.NMI_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        myTestCPU.getBus().signalNMI(true);
        myTestCPU.execute();
        // S was not set via an instrution so the NMI was ignored
        assertEquals(0xB01, myTestCPU.pc.intValue());

        myTestCPU.write_word(0xB00, 0x10CE); // LDS #$900 sets CC
        myTestCPU.write_word(0xB02, 0x900);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();
        assertEquals(0x1234, myTestCPU.pc.intValue());

        myTestCPU.getBus().signalNMI(false);
        myTestCPU.execute();
        assertEquals(0xB04, myTestCPU.pc.intValue());
        assertEquals(0x81, myTestCPU.cc.intValue()); // Flag E is set by IRQ
        assertEquals(0x01, myTestCPU.a.intValue());
        assertEquals(0x02, myTestCPU.b.intValue());
        assertEquals(0x900, myTestCPU.s.intValue());
        assertEquals(0x04, myTestCPU.read(0x900 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(0x900 - 2)); // Check that PC-high was pushed.
        assertEquals(0x09, myTestCPU.read(0x900 - 3)); // Check that U-low was pushed.
        assertEquals(0x08, myTestCPU.read(0x900 - 4)); // Check that U-high was pushed.
        assertEquals(0x07, myTestCPU.read(0x900 - 5)); // Check that Y-low was pushed.
        assertEquals(0x06, myTestCPU.read(0x900 - 6)); // Check that Y-high was pushed.
        assertEquals(0x05, myTestCPU.read(0x900 - 7)); // Check that X-low was pushed.
        assertEquals(0x04, myTestCPU.read(0x900 - 8)); // Check that X-high was pushed.
        assertEquals(0x03, myTestCPU.read(0x900 - 9)); // Check that DP was pushed.
        assertEquals(0x02, myTestCPU.read(0x900 - 10)); // Check that B was pushed.
        assertEquals(0x01, myTestCPU.read(0x900 - 11)); // Check that A was pushed.
        assertEquals(0x81, myTestCPU.read(0x900 - 12)); // Check that CC was pushed.
    }

    /**
     * Test fast IRQ pushing CC and PC registers on stack.
     */
    @Test
    public void raiseFastIRQ() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        myTestCPU.a.set(1);
        myTestCPU.b.set(2);
        myTestCPU.dp.set(3);
        myTestCPU.x.set(0x0405);
        myTestCPU.y.set(0x0607);
        myTestCPU.u.set(0x0809);

        myTestCPU.write_word(MC6809.FIRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        myTestCPU.getBus().signalFIRQ(true);
        myTestCPU.execute();
        assertEquals(0x1234, myTestCPU.pc.intValue());

        myTestCPU.getBus().signalFIRQ(false);
        myTestCPU.execute();
        assertEquals(0xB01, myTestCPU.pc.intValue());
        assertEquals(0x0F, myTestCPU.cc.intValue()); // Flag E is off by FIRQ
        assertEquals(0x01, myTestCPU.a.intValue());
        assertEquals(0x02, myTestCPU.b.intValue());
        assertEquals(517, myTestCPU.s.intValue());
        assertEquals(0x01, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
        assertEquals(0x0F, myTestCPU.read(517 - 3)); // Check that CC was pushed.
    }

    /**
     * Test CWAI.
     */
    @Test
    public void callCWAI() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        myTestCPU.a.set(1);
        myTestCPU.b.set(2);
        myTestCPU.dp.set(3);
        myTestCPU.x.set(0x0405);
        myTestCPU.y.set(0x0607);
        myTestCPU.u.set(0x0809);

        myTestCPU.write_word(MC6809.IRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x3C); // CWAI
        myTestCPU.write(0xB01, 0x12); // value for CC AND
        myTestCPU.write(0xB02, 0x12); // NOP
        myTestCPU.getBus().signalIRQ(true);
        myTestCPU.execute();
        assertEquals(0x1234, myTestCPU.pc.intValue());
        myTestCPU.getBus().signalIRQ(false);
        myTestCPU.execute();
        assertEquals(0xB02, myTestCPU.pc.intValue());
        assertEquals(0x82, myTestCPU.cc.intValue()); // Flag E is set by CWAI
        assertEquals(0x02, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
        assertEquals(0x09, myTestCPU.read(517 - 3)); // Check that U-low was pushed.
        assertEquals(0x08, myTestCPU.read(517 - 4)); // Check that U-high was pushed.
        assertEquals(0x07, myTestCPU.read(517 - 5)); // Check that Y-low was pushed.
        assertEquals(0x06, myTestCPU.read(517 - 6)); // Check that Y-high was pushed.
        assertEquals(0x05, myTestCPU.read(517 - 7)); // Check that X-low was pushed.
        assertEquals(0x04, myTestCPU.read(517 - 8)); // Check that X-high was pushed.
        assertEquals(0x03, myTestCPU.read(517 - 9)); // Check that DP was pushed.
        assertEquals(0x02, myTestCPU.read(517 - 10)); // Check that B was pushed.
        assertEquals(0x01, myTestCPU.read(517 - 11)); // Check that A was pushed.
        assertEquals(0x82, myTestCPU.read(517 - 12)); // Check that CC was pushed.
    }

    /**
     * Test that ignore IRQ works.
     */
    @Test
    public void ignoreIRQ() {
        myTestCPU.cc.setI(true);
        myTestCPU.write(0x1234, 0x12); // NOP
        myTestCPU.write(0x1235, 0x3B); // RTI
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        myTestCPU.write(0xB02, 0x12); // NOP
        myTestCPU.getBus().signalIRQ(true);
        myTestCPU.execute();
        assertEquals(0xB01, myTestCPU.pc.intValue());
        myTestCPU.cc.setI(false); // IRQ is still raised. Should trigger now
        myTestCPU.execute();
        assertEquals(0x1234, myTestCPU.pc.intValue());
        myTestCPU.execute();
        assertEquals(0x1235, myTestCPU.pc.intValue());
        assertTrue(myTestCPU.cc.isSetI()); // Check that I-flag is true.
        myTestCPU.getBus().signalIRQ(false);
        myTestCPU.execute();  // Get RTI
        assertFalse(myTestCPU.cc.isSetI()); // Check I-flag
    }

    /**
     * Test that IRQ is run again if it isn't lowered
     */
    @Test
    public void irqNotLowered() {
        myTestCPU.write(0x1234, 0x12); // NOP
        myTestCPU.write(0x1235, 0x3B); // RTI
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        myTestCPU.write(0xB02, 0x12); // NOP
        myTestCPU.getBus().signalIRQ(true);
        myTestCPU.execute();
        assertEquals(0x1234, myTestCPU.pc.intValue());
        myTestCPU.execute();  // Get NOP
        assertEquals(0x1235, myTestCPU.pc.intValue());
        assertTrue(myTestCPU.cc.isSetI()); // Check that I-flag is true.
        myTestCPU.execute();  // Get RTI
        assertEquals(0x1234, myTestCPU.pc.intValue());
        myTestCPU.execute();  // Get NOP
        myTestCPU.getBus().signalIRQ(false);
    }
}
