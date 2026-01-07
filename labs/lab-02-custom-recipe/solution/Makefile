CC ?= gcc
CFLAGS ?= -Wall -Wextra -O2
LDFLAGS ?=

PREFIX ?= /usr
DESTDIR ?=
bindir = $(PREFIX)/bin

TARGET = hello-world
SRCS = hello-world.c
OBJS = $(SRCS:.c=.o)

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $^

%.o: %.c
	$(CC) $(CFLAGS) -c -o $@ $<

install: $(TARGET)
	install -d $(DESTDIR)$(bindir)
	install -m 0755 $(TARGET) $(DESTDIR)$(bindir)/

clean:
	rm -f $(TARGET) $(OBJS)

.PHONY: all install clean
