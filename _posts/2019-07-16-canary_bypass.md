---
title: "Canary Bypass"
date: 2019-07-16 20:33:35 -0700
categories: [linux_exploit_dev]
tags: [exploit_walkthrough]     # TAG names should always be lowercase
---

Linux binary exploitation is a fascinating topic. A big thanks to my good friend [vampire](https://dhavalkapil.com/), who provided me with this challenge and guided me through a complex stack canary bypass technique.

The primary objective of this challenge is to execute `/bin/bash` through `system()` function.


## Vulnerable Code

```c
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

struct contact {
  char name[20];
  char *description;
};

void setup() {
  setvbuf(stdin, NULL, _IONBF, 0);
  setvbuf(stdout, NULL, _IONBF, 0);
  setvbuf(stderr, NULL, _IONBF, 0);
}

int main() {
  struct contact c;

  setup();
  memset(&c, 0, sizeof(c));
  c.description = malloc(100);
  puts("Enter name:");
  scanf("%s", c.name);
  puts("Enter description:");
  scanf("%s", c.description);
  return 0;
}
```

Instruction to compile the code via `gcc`.

```bash
gcc vuln.c -o vuln

# Reference: https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/Makefile
# compile the code using Makefile with default gcc protections
make all
```


## My Exploit Dev Environment & Tools

- Linux ubuntu 4.4.0-186-generic, 64 bit Server 16.0.4.7
- GLIBC 2.23
- Python 3.9.1, pwndbg
- Tmux, ROPgadget, IDA64_v8.4 freeware, Objdump, checksec


## OS & Binary Protections

Get the `ASLR` status of my environment.
```bash
╰─➤  cat /proc/sys/kernel/randomize_va_space
2
```

> - `randomize_va_space = 0` means ASLR is disabled.
> - `randomize_va_space = 1` means ASLR is enabled with stack, virtual dynamic shared object (VDSO) page and shared memory regions. As a result, libc addresses are `randomized` on every program `restart`.
> - `randomize_va_space = 2` means ASLR is enabled with all capabilities in `addition` with the `data segments`. This is the most `secure` option.
{: .prompt-info }


> However, ASLR protection only provides `28 bits of entropy`.
> - The first `24` bits and the last `12` bits remain `constant` or fixed
> - Only middle `28` bits are randomized.
{: .prompt-danger}


Now explore the default `buffer overflow` protections with `checksec` utility in the binary(i.e `vuln`) compiled with `gcc` default settings.

```bash
╰─➤  checksec vuln
[*] '/home/asinha/linux_exploit_dev/exploits/vampire/canary_bypass/vuln'
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)
```


1. `amd64-64-little` indicates that it is a 64-bit `ELF` binary.
2. `Partial RELRO` indicates that `.GOT` is `readable` and `writable`.
3. `Canary found` indicates that `canary` is used to detect a stack smashing attack. With each program restart, this `8-byte` `random` value changes. For reference, canary value always starts with `\x00`.
4. `NX enabled` indicates that `stack` memory is not `executable`.
5. `No PIE (0x400000)` indicates that `.bss` and `.data` sections are `not randomized` on each program restart. For reference, the .GOT is located within the data segment (RW) section.


> In contrast, PIE binaries support ASLR , allowing the entire binary to be loaded at a random address each time. This applies randomness to sections like `.data` and `.bss`, but only when PIE is enabled.
{: .prompt-tip}


In summary,

1. The `vuln` binary should be consistently loaded from a fixed address. However, due to the more secure ASLR settings, libc addresses are randomized with each program restart, preventing us from directly hardcoding any libc addresses in our payload.

2. .PLT and .GOT table address are fixed with each program restart and we can also write on those addresses.

## Code Review

1. The variable `c`, which is of type `structure` with the tag `contact`, is declared in main(), so it is stored on the stack. In a 64-bit system, the block size is 8 bytes, resulting in a total structure size of 32 bytes.
   - `name` = 24 bytes (originally allocated 20 bytes in the code, but the GCC compiler rounds up to the nearest multiple of 8)
   - `description` = 8 bytes
2. The `setup()` function is used solely to flush the buffer.
3. `memset()` function is used to `zero out` the structure variables.
4. `scanf()` reads from the `stdin` based on the format specifier. Here the format specifier is `%s` so it reads the input as `string` until we press `ENTER` or provide `\n`, then it stores that entire value into the variables (i.e. `c.name` and `c.description`) and adds a `null` byte or `\0` at the end.
5. `c.description` variable holds an address that points to a `heap` region.

## The Bugs

### Bug 1

There is no boundary checking when the code takes inputs from the user.

It is possible to overwrite the `stack` memory by providing more than 20 bytes of input through `name` variable.

```text
line 23: scanf("%s", c.name);  => %19s is not used
```

### Bug 2

It is also possible to overwrite/ overflow the adjacent `heap` memory by providing more than 100 bytes input through  `description` variable.

```text
line 25: scanf("%s", c.description);  => %99s is not used
```

If we `chain` those two bugs, then we can write anything in our `specified` address.

1. While giving input through the `name` variable, we can overwrite the `heap_pointer` with an `address`.
2. While providing input through the `description` variable, we can write something in that specified address pointed by the `heap_pointer`.

## Static Analysis

Find the entry point of that binary.

```bash
╰─➤  readelf -a vuln | grep -i "entry"
  Entry point address:               0x4006a0
```

Since the binary is `non-PIE`, it is loaded into memory from the fixed address `0x4006a0` each time.


Understand the assembly of the binary

```bash
objdump -d -M intel vuln
```

![assembly](assets/2019-07-16-canary_bypass.assets/assembly.png)

## Dynamic Analysis

To verify the memory layout during the first `scanf()` call inside the  `main()` frame, lets send input via python.
- 24 bytes `A` buffers via `name` variable and
- 8 bytes `B` buffers via `description` variable.

Run the following python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit01.py) under `tmux` session to interact with the binary via `pwndbg`.


![exploit01_1](assets/2019-07-16-canary_bypass.assets/exploit01_1.png)

- Program automatically halts at the first `scanf()` statement.
- `[DEBUG] sent 0x18 bytes` : This statement implies that the exploit code has sent out the input / buffer but the program does not yet receive it.
- `Backtrace` section tells the execution is not in the `main()` stack frame.

Jump into the `main()` frame by entering the `finish` command 5 times.

![exploit01_2](assets/2019-07-16-canary_bypass.assets/exploit01_2.png)

From our static analysis, we knew that

- `RSP + 0` offset points at the `name` variable
- `RSP + 24` offset points at the `description` variable or `heap_pointer`

Verify this hypothesis by analyzing the memory stack from `RSP`.

```
# examine 20 "8 bytes of memory chunks" in hex format
x/20gx $rsp
```

![exploit01_3](assets/2019-07-16-canary_bypass.assets/exploit01_3.png)


- `ASLR` is activated in the host therefore this `libc` address changes in every program `restart`. However we can always get a libc address from a fixed offset `[RSP + 8]`
- We can't `leak` the `canary` value because there are no `puts()` / `printf()` after the last `scanf()`.
- The binary has partial RELRO, allowing us to overwrite entries in the .GOT.
  - Additionally, since this is a non-PIE binary, the .GOT address is fixed.


## Global Offset Table / GOT

- Whenever `__stack_chk_fail` is called, control transfers to the `.GOT` via the `.PLT`. The value or libc address stored in the .GOT is then loaded into `RIP`.

![stack_check_fail_got](assets/2019-07-16-canary_bypass.assets/stack_check_fail_got.png)

- The .GOT addresses, such as `0x0000000000601018`(for `puts()`) and `0x0000000000601020`(for `__stack_chk_fail`), remain fixed on each program restart. However, the values stored at these addresses will vary with each restart.

- Run the following python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit02.py) to corrupt the canary and observe the values stored on that locations.

- Set two breakpoints
  - first before the calling the  of `__stack_chk_fail()`@plt
  - second at `__stack_chk_fail` function.

```
pwndbg> disassemble main
pwndbg> b *0x0000000000400890
break __stack_chk_fail
```

![got_analysys_1](assets/2019-07-16-canary_bypass.assets/got_analysys_1.png)

`c` or `continue` the program.

![got_analysys_2](assets/2019-07-16-canary_bypass.assets/got_analysys_2.png)

When we hit our first breakpoint, we observe that `__stack_chk_fail()@got` -> `0x601020` does not yet have a libc address. This is expected, as it hasn’t been called yet.

In contrast, `puts()@got` -> `0x601018` does have a libc address, as puts() has already been called.

Now when we `continue` the program again, then the execution goes to `__stack_chk_fail.plt` -> `plt_common_stub` -> `dll_runtime_resolv_avx` and finally `resolve` the libc address.

The execution stops at the `second` breakpoint when it is **about to call** `__stack_chk_fail`.

At this point, it is noticed that `__stack_chk_fail@got` -> `0x601020` gets a libc address.

![got_analysys_3](assets/2019-07-16-canary_bypass.assets/got_analysys_3.png)


## Control RIP

To gain control of `RIP`, we need to overwrite the value (essentially a libc address) stored at `0x0000000000601020` during runtime with canary corruption.

**`WHERE` to write**

- While providing the input through the `name` variable, we can overwrite the `heap_pointer` value with `0000000000601020`.
- Also, we need to send minimum `48` bytes buffer to corrupt the `canary` stored on the stack so that it `triggers` the `__stack_chk_fail` function.

**`WHAT` to write**

- While providing input through the `description` variable, we can feed `0xdeadbeef`.
- Objective is to set `RIP` = `0xdeadbeef` when we continue to execute the program.
- Later we can update this `0xdeadbeef` with `ROP` gadgets to invoke `system()` function.

### Enter into the Bad Land

Usually, the following characters are considered bad.
- `0x0a` represents `\n`
- `0x0d` represents `\t`
- `0x20` represents `space`


Unfortunately, `0x0000000000601020` address has a bad char(i.e `0x20`). As a result, we can't enter this address through our `name` variable.

### Solution

In ASLR protection, the last 12 bits remain fixed, allowing us to reliably predict that the last byte of any libc address will be `\x00`.

Through `name` variable, rather than overwriting the heap_pointer value with `0x0000000000601020`, we can use `0x000000000060101f` instead.
  - This address contains no bad characters.
  - It is just one byte before the previous address, meaning it targets the last byte of the `puts@GOT` address.

Now while providing the input through `description` variable,
  - First we need to restore the puts@GOT byte and set it to `\x00`.
  - Then set the `__stack_chk_fail@got` entry to `0xdeadbeef`.
  - Therefore, we need to feed `\x00deadbeef` via `description` variable.

Run the following python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit03.py) to corrupt the canary and control `rip`.

![exploit03_1](assets/2019-07-16-canary_bypass.assets/exploit03_1.png)

It is observed that during the crash `rsp` points just before our `name` buffer.

In order to get full control on the stack, we need make sure at the very `minimum` rsp should point to `pad` block(i.e before canary block).

### ESP Adjustment via ROP

Try to find (6 `POP`) or (5 `POP` + `RETN`) gadget `address`.

```bash
ROPgadget --binary vuln | grep pop
```

![rop_stack_adjustment](assets/2019-07-16-canary_bypass.assets/rop_stack_adjustment.png)

Select `0x00000000004008fb`: 5 `POP` + `RETN`.
- `RETN` means (`pop rip`, `jmp rip`).

Run the following python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit04.py) to to feed `0x00000000004008fb` address via `description` variable and overwrite the 8 bytes `pad block` address with `0xdeadbeef` to gain control on the `rip` again.

![exploit04_1](assets/2019-07-16-canary_bypass.assets/exploit04_1.png)


## Leak a libc address

Now our objective is to leak `any libc address` and find the offset from `system()`.

`puts()` is used in our program code, allowing us to leak any libc address by calling the `puts()` function if we know its address.

There are two ways,

### puts@PLT

- This address is located in the code section, so it is fixed with RX permissions.

Open the binary using Ida, click on the  `_puts` in
"Function" left side window and select "text view".

![puts_plt](assets/2019-07-16-canary_bypass.assets/puts_plt.png)

`puts@plt` address `0x400620` contains a bad char(i.e `\x20`) so we can't select that address.

### puts@GOT

- This address is also in the code section and remains fixed with RW permissions because of partial_relro.

![puts_got](assets/2019-07-16-canary_bypass.assets/puts_got.png)

`0x601018` address does not hane any bad chars. Therefore we can safely use this address in our exploit code.

### Argument of puts()

In order to print a string, `puts()` takes only one argument - a `pointer` to a `string`.

![puts_manual](assets/2019-07-16-canary_bypass.assets/puts_manual.png)

To pass the arguments to `puts()` function, we need to load the `rdi` register with an address we want to leak.

Search a gadget for `POP RDI; RETN`

![rop_setup_rdi](assets/2019-07-16-canary_bypass.assets/rop_setup_rdi.png)

- As we know, all libc addresses are loaded into the .GOT during program runtime. To leak the address of a function in libc, we need to pass its corresponding .GOT address as an argument to the puts() function.
- The puts() function then prints the values from that address until it encounters a NULL character.
- We can select any libc address from the .GOT. For instance, let’s choose malloc@got at `0x601038`, since malloc@GOT is resolved before puts.

Putting all concepts together
1. We can set that pad block with set up `rdi gadget` address `0x400903` instead of `0xdeadbeef`.
2. Set the next canary block with `malloc@got` => `0x601038` as we want to leak the malloc's libc address.
3. Set next `rbp` block with `puts@plt` => `0x400620`.
4. Set next `retn` block with `0xdeadbeef` to control the `rip` again.

### Libc CSU_init Gadget

The previously identified `rdi gadget` address proved unusable because `\x09` is a bad character. As a result, the exploit code provided below failed to execute as intended.

![pop_rdi_retn](assets/2019-07-16-canary_bypass.assets/pop_rdi_retn.png)


> `__libc_csu_init` is part of the C runtime setup code in ELF binaries. Its purpose is to initialize the C Standard Library (CSU) before the program’s main function is executed.
{: .prompt-info }

We can use this function to set up rdi / edi register.

![mov_edi_r15d_gadget](assets/2019-07-16-canary_bypass.assets/mov_edi_r15d_gadget.png)

Lets understand the dependencies first

1. rdi <= edi <= r15, therefore we need to set the r15 register
2. `0x00000000004008e9 <+73>:    call   QWORD PTR [r12+rbx*8]`, if we control r12 and rbx then we can jump into our desired address.
  - If we can set rbx=0 then jump only depends on r12.
3. In `0x4008f4` there is a `jne` insturction that depends on the value of rbx and rbp.

![csu_gadget_plan](assets/2019-07-16-canary_bypass.assets/csu_gadget_plan.png)

We can start at `0x00000000004008fa` but before that we need set up the stack in the following way

```text
0x4141414141414141 => AAAAAAAA
0x4141414141414141 => AAAAAAAA
0x4141414141414141 => AAAAAAAA
0x000000000060101f =>  heap_ptr -> put@got last byte
0x00000000004008FA => csu gadget
0x0000000000000000 => pop rbx # set rbx 0
0x0000000000000001 => pop rbp # set rbp 1 bypass jne
0x0000000000601018 => pop r12 # set r12 puts@GOT
0x0000000000000002 => pop r13 # set r13 any value
0x0000000000000002 => pop r14 # set r14 any value
0x0000000000601038 => pop r15 # set r15 malloc@GOT -> set the value to rdi
0x00000000004008E6 => retn # overwrite with second csu gadget -> mov  edi,r15d
0x0000000000000002 => set any value to accommodate 0x4008f6: add    rsp, 8
0x0000000000000002 => set any value to accommodate pop rbx
0x0000000000000002 => set any value to accommodate pop rbp
0x0000000000000002 => set any value to accommodate pop r12
0x0000000000000002 => set any value to accommodate pop r13
0x0000000000000002 => set any value to accommodate pop r14
0x0000000000000002 => set any value to accommodate pop r16
p64(0xdeadbeef) => retn # control eip
```

- When the control jumps back from `puts()`, it will start the execution from `0x4008ed`. Therefore, we need to make sure that rbx == rbp to bypass the `jne` instruction and for that we need to set rbp = 1.
- After `jne`, it executes all pop instructions untill hit the retn.


Run the following python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit05.py) to control the rip and leak the malloc libc value.

Verify the same malloc libc address in the gdb context using `telescope 0x601018`.

![libc_malloc_leak](assets/2019-07-16-canary_bypass.assets/libc_malloc_leak.png)

### Calculate the libc base address

> Using this `libc_leak`, we can determine the offsets of the `system()`, `malloc` or any functions and the `/bin/sh` string within the libc library.
> These offsets are constant for that specific version of libc and remain unchanged across runs.
{: .prompt-info }

Get the malloc offset from the libc base.

```bash
objdump -T /lib/x86_64-linux-gnu/libc.so.6 | grep -i malloc
```

![malloc_offset](assets/2019-07-16-canary_bypass.assets/malloc_offset.png)

Run the python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit06.py) to leak the libc base address.

![libc_base_address](assets/2019-07-16-canary_bypass.assets/libc_base_address.png)

The presence of three trailing zeroes in the address indicates that it could be associated with the libc library.

## Preparing to call system()

### Argument of System()

`system` function takes one argument a char to a string. For example the address of `/bin/sh` string.

![system_function](assets/2019-07-16-canary_bypass.assets/system_function.png)

### Find /bin/sh offset from libc

```bash
strings -t x /lib/x86_64-linux-gnu/libc.so.6 | grep -i /bin/sh
```

![bin_sh_offset](assets/2019-07-16-canary_bypass.assets/bin_sh_offset.png)

Therefore, `/bin/sh` string address = libc_base + bin_sh_offset(`0x18ce57`)

### Find the system() base address

Similarly, as demonstrated earlier, we can use objdump to determine the `offset` of the system function from the libc base address.

```bash
objdump -T /lib/x86_64-linux-gnu/libc.so.6 | grep -i system
```

![system_function_offset](assets/2019-07-16-canary_bypass.assets/system_function_offset.png)

Therefore, `system` function address = libc_base + system_function_offset(`0x00000000000453a0`)

At this point, we have all information to call `system('\bin\sh')`.
However, the core issue is that we cannot control our input at this stage.

Therefore. if we jump back to `main()`, then again, we can control our input.

1. We need to replace `0xdeadbeef` with the address of `main()`.
2. We need to take control of the `rip` again through `canary` corruption, but this time, during stack step up, we call `system('\bin\sh')` directly instead of calling `puts()`.
3. In order to set the argument of `system` function, we need to load the address of the `/bin/sh` string into the RDI register.
4. As we already have the libc leak, therefore we can directly find the gadget - `pop rdi, retn` from libc.

```bash
ROPgadget --binary /lib/x86_64-linux-gnu/libc.so.6 | grep "pop rdi" | grep "ret"
...
0x0000000000021112 : pop rdi ; ret
...
```

### Find the address of main()

```bash
pwndbg> print main
$2 = {<text variable, no debug info>} 0x4007f7 <main>
```

## The Shell

Run the python [code](https://github.com/greyshell/linux_exploit_dev/blob/main/exploits/vampire/canary_bypass/exploit07.py) to anticipate a shell.

![shell](assets/2019-07-16-canary_bypass.assets/shell.png)

## References

- Code Repo: <https://github.com/greyshell/linux_exploit_dev/tree/main/exploits/vampire/canary_bypass>
