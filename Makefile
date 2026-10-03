CC = gcc
CFLAGS = -std=c11 -Wall -Wextra -Wpedantic -Werror -O2
BUILD = build
MONITOR = Monitor-20261002T192224Z-1-001/Monitor
PROGRAMS = $(BUILD)/fork2 $(BUILD)/pipe2 $(BUILD)/peterson $(BUILD)/prod_cons

.PHONY: all test java run-fork run-pipe run-peterson run-prod-cons run-java-3b run-java-3s
all: $(PROGRAMS)

$(BUILD):
	mkdir -p $@

$(BUILD)/fork2: fork2.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@

$(BUILD)/pipe2: pipe2.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@ -lm

$(BUILD)/peterson: peterson-code.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@ -pthread

$(BUILD)/prod_cons: prod_cons.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@ -pthread

test: all
	python3 tests/test_examples.py

java: $(BUILD)/java-3b/.compiled $(BUILD)/java-3s/.compiled

$(BUILD)/java-3b/.compiled: $(wildcard $(MONITOR)/1/*.java)
	mkdir -p $(BUILD)/java-3b
	javac -d $(BUILD)/java-3b $^
	touch $@

$(BUILD)/java-3s/.compiled: $(wildcard $(MONITOR)/3/*.java)
	mkdir -p $(BUILD)/java-3s
	javac -d $(BUILD)/java-3s $^
	touch $@

run-fork: $(BUILD)/fork2
	./$(BUILD)/fork2
run-pipe: $(BUILD)/pipe2
	./$(BUILD)/pipe2
run-peterson: $(BUILD)/peterson
	./$(BUILD)/peterson
run-prod-cons: $(BUILD)/prod_cons
	./$(BUILD)/prod_cons
run-java-3b: java
	java -cp $(BUILD)/java-3b ThreadSync3b
run-java-3s: java
	java -cp $(BUILD)/java-3s ThreadSync3s

.PHONY: test-simulation run-simulation
test-simulation:
	mkdir -p $(BUILD)/simulation-tests
	java -m jdk.compiler/com.sun.tools.javac.Main -d $(BUILD)/simulation-tests pc_trabalho04_202011393/src/main/java/br/edu/mata58/dinheiro/BoundedBuffer.java pc_trabalho04_202011393/src/main/java/br/edu/mata58/dinheiro/Simulation.java pc_trabalho04_202011393/src/test/java/br/edu/mata58/dinheiro/SimulationTest.java
	java -cp $(BUILD)/simulation-tests br.edu.mata58.dinheiro.SimulationTest
run-simulation:
	mvn -f pc_trabalho04_202011393/pom.xml javafx:run

.PHONY: run-pipe-soma
$(BUILD)/pipe-soma: examples/dia05/pipe_soma.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@
run-pipe-soma: $(BUILD)/pipe-soma
	./$(BUILD)/pipe-soma

.PHONY: run-fork-basico run-memoria
$(BUILD)/fork-basico: examples/dia05/fork_basico.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@
$(BUILD)/memoria-independente: examples/dia05/memoria_independente.c | $(BUILD)
	$(CC) $(CFLAGS) $< -o $@
run-fork-basico: $(BUILD)/fork-basico
	./$(BUILD)/fork-basico
run-memoria: $(BUILD)/memoria-independente
	./$(BUILD)/memoria-independente
