---
typora-copy-images-to: .assets
typora-root-url: .
---

# DataFlow Diagrams

## DataFlow Diagram (DFD) Symbols

A data flow diagram shows how data is processed within a system based on inputs and outputs. Visual symbols are used to represent the flow of information, data sources and destinations, and where data is stored. Data flow diagrams are often used as a first step toward redesigning a system. They provide a graphical representation of a system at any level of detail, creating an easy-to-understand picture of what the system does. A general overview of a system is represented with a context diagram, also known as a level 0 DFD, which shows a system as a single process. A level 1 diagram provides greater detail, focusing on a system’s main functions. Diagrams that are level 2 or higher illustrate a system’s functioning with increasing detail. It’s rare for a DFD to go beyond level 2 because of the increasing complexity, which makes it less effective as a communication tool.

![Sample DataFlow Diagram](/.assets/data-flow-diagram-sample.svg)

## DataFlow Diagram (DFD) Notations

The two main types of notation used for data flow diagrams are Yourdon-Coad and Gane-Sarson, both named after their creators, all experts who helped develop DFD methodology: Ed Yourdon, Peter Coad, Chris Gane and Trish Sarson. There are some differences in style between the notation types. For example, Yourdon and Coad notation uses circles to represent processes, whereas Gane and Sarson notation use rectangles with rounded corners. Another variation is the symbol used for data stores—Yourdon and Coad uses parallel lines while Gane and Sarson notation uses an open-ended rectangle. Because DFD symbols vary, it’s important to be consistent with whatever notation you choose in order to avoid confusion. If you’re using DFD software, it will likely dictate which set of symbols are available to use.

All data flow diagrams include four main elements: entity, process, data store and data flow.

**External Entity** – Also known as actors, sources or sinks, and terminators, external entities produce and consume data that flows between the entity and the system being diagrammed. These data flows are the inputs and outputs of the DFD. Since they are external to the system being analyzed, these entities are typically placed at the boundaries of the diagram. They can represent another system or indicate a subsystem.

**Process** – An activity that changes or transforms data flows. Since they transform incoming data to outgoing data, all processes must have inputs and outputs on a DFD. This symbol is given a simple name based on its function, such as “Ship Order,” rather than being labeled “process” on a diagram. In Gane-Sarson notation, a rectangular box is used and may be labeled with a reference number, location of where in the system the process occurs and a short title that describes its function. Processes are typically oriented from top to bottom and left to right on a data flow diagram.

**Data Store** – A data store does not generate any operations but simply holds data for later access. Data stores could consist of files held long term or a batch of documents stored briefly while they wait to be processed. Input flows to a data store include information or operations that change the stored data. Output flows would be data retrieved from the store.

**Data Flow** – Movement of data between external entities, processes and data stores is represented with an arrow symbol, which indicates the direction of flow. This data could be electronic, written or verbal. Input and output data flows are labeled based on the type of data or its associated process or data store, and this name is written alongside the arrow.



|                 | Yourdon and Coad                                             | Gane and Sarson                                              |
| --------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| External Entity | ![](/.assets/data-flow-symbol-yourdon-coad-external-entity.svg) | ![](/.assets/data-flow-symbol-gane-sarson-external-entity.svg) |
| Process         | ![](/.assets/data-flow-symbol-yourdon-coad-process.svg)      | ![](/.assets/data-flow-symbol-gane-sarson-process.svg)       |
| Data Store      | ![](/.assets/data-flow-symbol-yourdon-coad-data-store.svg)   | ![](/.assets/data-flow-symbol-yourdon-coad-data-store.svg)   |
| Data Flow       | ![](/.assets/data-flow-symbol-data-flow.svg)                 | ![](/.assets/data-flow-symbol-data-flow.svg)                 |





## References

* https://www.lucidchart.com/pages/data-flow-diagram/data-flow-diagram-symbols