Creative Micro Systems 9639
===========================

* OS-9 Level II
* The DEFS files come from FM-11.

The OS-9 kernel was installed in a 4 KB ROM that must be mapped to $F000 at CPU reset. The Boot binary starts at $0000 in the ROM. OS9p1 must be located so the top matches $0FFF in the ROM. There is a gab of 56 bytes between them.

After installing the main package, install from os9software folder with:

```
cd os9software
mvn install -Dpackage=cms9639
```

These files are added to the cms9639 folder before the build and deleted immediately afterward:
- "dragon128/OS9p2_ed13.asm"
- "kernel/Acia51_ed6.asm"   
- "dragon128/IOMan_ed10.asm"
- "dragon128/Shell_ed21.asm"
- "positron9000/SCF.asm"    
- "positron9000/RBF.asm"    
- "kernel/ClockL2_ed3.asm"  

For more information see http://www.sardis-technologies.com/oth6809/cms9639.htm
