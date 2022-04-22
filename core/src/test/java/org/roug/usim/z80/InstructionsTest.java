package org.roug.usim.z80;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.roug.usim.Bus8Intel;

public class InstructionsTest extends Framework {

    private static final int LOCATION = 0x1e20;


    /**
     * Load a short program into memory.
     * Note: sets the CC to 0.
     */
    private void loadProg(int[] instructions) {
        for (int i = 0; i < instructions.length; i++) {
            myTestCPU.write(i + LOCATION, instructions[i]);
        }
        myTestCPU.reset();
    }

    @Test
    public void testNOP() {
        setA(0x8B);
        myTestCPU.write(0x0B00, 0x00); // NOP instruction
        myTestCPU.write(0x0B01, 0x00); // NOP instruction
        execSeq(0xB00, 0xB01);
        assertEquals(0x8B, myTestCPU.registerA.intValue());
    }

    /* Complement Accumulator */
    @Test
    public void testCPL() {
        setA(0x8B);
        myTestCPU.registerF.clear();
        myTestCPU.write(0x0B00, 0x2F); // CPL instruction
        execSeq(0xB00, 0xB01);
        assertEquals(0x74, myTestCPU.registerA.intValue());
        assertEquals(0x12, myTestCPU.registerF.get());
    }

    /* Negate Accumulator
     * TODO:Complete
     */
    @Test
    public void testNEG() {
        setA(0xED);
        myTestCPU.registerF.clear();
        myTestCPU.write(0x0B00, 0xED); // NEG instruction
        myTestCPU.write(0x0B01, 0x44); // NEG instruction
        execSeq(0xB00, 0xB02);
        assertEquals(0x13, myTestCPU.registerA.intValue());
        assertEquals(0x12, myTestCPU.registerF.get()); // Not verified
    }

    @Test
    public void testEXX() {
        myTestCPU.registerBC.set(0x445A);
        myTestCPU.registerDE.set(0x3DA2);
        myTestCPU.registerHL.set(0x8859);
        myTestCPU.registerBC_.set(0x0988);
        myTestCPU.registerDE_.set(0x9300);
        myTestCPU.registerHL_.set(0x00E7);
        myTestCPU.registerF.clear();
        myTestCPU.write(0x0B00, 0xD9); // EXX instruction
        execSeq(0xB00, 0xB01);
        assertEquals(0x0988, myTestCPU.registerBC.get());
        assertEquals(0x9300, myTestCPU.registerDE.get());
        assertEquals(0x00E7, myTestCPU.registerHL.get());
        assertEquals(0x445A, myTestCPU.registerBC_.get());
        assertEquals(0x3DA2, myTestCPU.registerDE_.get());
        assertEquals(0x8859, myTestCPU.registerHL_.get());

        assertEquals(0x00, myTestCPU.registerF.get());
    }

    @Test
    public void loadSPfromHL() {
        myTestCPU.registerSP.set(0x245A);
        myTestCPU.registerHL.set(0x442E);
        writeSeq(0x0B00, 0xF9);
        execSeq(0xB00, 0xB01);
        assertEquals(0x442E, myTestCPU.registerSP.get());
    }

    @Test
    public void loadBCImmediate() {
        myTestCPU.registerBC.set(0x445A);
        writeSeq(0x0B00, 0x01, 0x00, 0x50);
        execSeq(0xB00, 0xB03);
        assertEquals(0x5000, myTestCPU.registerBC.get());
    }

    /* Test load of stack pointer */
    @Test
    public void loadSPImmediate() {
        myTestCPU.registerSP.set(0x445A);
        writeSeq(0x0B00, 0x31, 0x00, 0x50);
        execSeq(0xB00, 0xB03);
        assertEquals(0x5000, myTestCPU.registerSP.get());
    }

    @Test
    public void pushRegisterPairBC() {
        myTestCPU.registerSP.set(0x400);
        myTestCPU.registerBC.set(0x445A);
        writeSeq(0x0B00, 0x65);
        execSeq(0xB00, 0xB01);
        assertEquals(0x3FE, myTestCPU.registerSP.get());
        assertEquals(0x445A, myTestCPU.registerBC.get());
        assertEquals(0x445A, myTestCPU.read_word(0x3FE));
    }

    @Test
    public void pushRegisterPairAF() {
        myTestCPU.registerSP.set(0x1007);
        myTestCPU.registerAF.set(0x2233);
        writeSeq(0x0B00, 0xF5);
        execSeq(0xB00, 0xB01);
        assertEquals(0x1005, myTestCPU.registerSP.get());
        assertEquals(0x2233, myTestCPU.registerAF.get());
        assertEquals(0x33, myTestCPU.read(0x1005));
        assertEquals(0x22, myTestCPU.read(0x1006));
    }

    /* Test CPU reset
     * TODO:Complete
     */
    @Test
    public void testRESET() {
        setPC(0xB00);
        myTestCPU.reset();
        assertEquals(0x0000, myTestCPU.pc.intValue());
    }

    @Test
    public void generateIRQ() {
        myTestCPU.write(0x0B00, 0x00); // NOP instruction
        myTestCPU.write(0x0B01, 0x00); // NOP instruction
        setPC(0xB00);
        Bus8Intel bus = myTestCPU.getBus();
        bus.signalIRQ(true);
        myTestCPU.execute();
        //assertEquals(0x0000, myTestCPU.pc.intValue());
        bus.signalIRQ(false);
    }

}
