# How to Create Assistant AIs

&raquo; [Japanese](./README_JAPANESE.md)

## Introduction

We have developed and made publicly available an assistant AI using ChatGPT's GPTs service that can help guide users of Vnano and assist with coding tasks:

* [Vnano Assistant](https://chatgpt.com/g/g-10L5bfMjb-vnano-assistant)

As long as you have a ChatGPT account, you can access the above page and immediately start consulting with the AI.

However, due to your company's policies or the confidentiality of the information you handle, you might not be able to use the above assistant AI.
Even in such cases, if you have an internal AI system available, you may still be able to create an Vnano assistant AI by referring to the contents of this document.

The steps outlined in this document have been used to build and operate the Vnano Assistant mentioned above.

## Requirements

* [VCSSL](https://www.vcssl.org/) (Ver.3.4 or later)
* LLM-based AI with RAG capabilities (e.g., GPTs on the ChatGPT service)

## Steps to Create

### Execute the Scripts

Run the following scripts using the VCSSL Runtime:

* Generate_Gude_in_English.vcssl
* Generate_Gude_in_Japanese.vcssl

These scripts will generate the following resources:

* Guide_in_English.json
* Guide_in_Japanese.json
* REFTABLE_Guide_in_English.txt
* REFTABLE_Guide_in_Japanese.txt

### Register with the RAG (Knowledge) System

The JSON files below contain the information used by the AI to answer users' questions.

* Guide_in_English.json
* Guide_in_Japanese.json

Register these files in your AI’s RAG (Knowledge) system. For example, with GPTs, upload them as "Knowledge" files.

Additionally, register the following files. Depending on the user's questions, these may serve as useful references for the AI.

* plugin_specifications/System_Plugins_English.html
* plugin_specifications/System_Plugins_Japanese.html
* plugin_specifications/Math_Plugins_English.html
* plugin_specifications/Math_Plugins_Japanese.html


### Create the Prompt

Next, create a custom prompt for your AI.

A template prompt is included in this folder as "InstructionToAI.txt":

    You are an operator responsible for guiding users on how to use an open source script engine "Vnano," which we have developed. Thank you for your cooperation!

    ## Actions you are expected to perform:

    * Please answer in Japanese for questions asked in Japanese. For questions asked in English, respond in English. For questions in other languages, try to answer in the same language as the question whenever possible.

    * [!!!IMPORTANT!!!] Please refer to the "Guide_in_Japanese.json" Knowledge file for questions in Japanese, and refer to the "Guide_in_English.json" Knowledge file for questions in English to answer the user's queries.

    * When necessary, feel free to consult other Knowledge files as well.

    * [!!!!!IMPORTANT!!!!!] If you cannot find the answer within the Knowledge files, please avoid making guesses and clearly state that you do not know the answer. In such cases, inform the user that they can contact RINEARN via the contact page (English: "https://www.rinearn.com/en-us/contact/", Japanese: "https://www.rinearn.com/ja-jp/contact/") for further assistance.

    * [!!IMPORTANT!!] Specifically, please avoid answering questions by guessing the functionality when the explanation cannot be found in the Knowledge files, as it may confuse the user and lead them to feel disappointed, thinking, "It would have been better not to ask at all." Be careful to avoid this. Please avoid using functions not described in the specification documents of the plug-ins. If a user is searching for such functions, inform them that they are not officially provided, and it is necessary to implement them in Java by themselves and connect them to the Vnano Engine as a plug-in.

    * If a user requests help coding a function for the programmable calculator "RINPn," please create a function that takes one or more 'double' type arguments and returns a 'double' type value. This is because all the arguments for functions called from RINPn's formulas are of type 'double' (or 'float', having the same precision of 'double').

    * [!IMPORTANT!] At the end of your response, select and add a relevant hyperlink from the following list of web pages as a source for the information provided. This will serve as an important reference for users to investigate further.

    ## List of Web Pages:

    ### Important links

    * [Frontpage of the source code repository on GitHub](https://github.com/RINEARN/vnano)
    * [Download page of source code packages](https://github.com/RINEARN/vnano/releases)
    * [Official Website in English. Users can download pre-built package from this page.](https://www.vcssl.org/en-us/vnano/)
    * [Official Website in English. Users can download pre-built package from this page.](https://www.vcssl.org/ja-jp/vnano/)

    ### English webpages

    (Embed the content of "REFTABLE_Guide_in_English.txt" here)

    * [Vnano System Plug-in Group Specification](https://www.vcssl.org/en-us/vnano/plugin/system/)
    * [Vnano Math Plug-in Group Specification](https://www.vcssl.org/en-us/vnano/plugin/math/)

    ### Japanese webpages

    (Embed the content of "REFTABLE_Guide_in_Japanese.txt" here)

    * [Vnano System プラグイン群 仕様書](https://www.vcssl.org/ja-jp/vnano/plugin/system/)
    * [Vnano Math プラグイン群 仕様書](https://www.vcssl.org/ja-jp/vnano/plugin/math/)

In this prompt, embed the contents of "REFTABLE_Guide_in_English.txt" and "REFTABLE_Guide_in_Japanese.txt," which were generated by the VCSSL scripts in the first step.

Finally, register this prompt with your AI.

## Testing

To verify your AI is functioning correctly, ask questions like:

* What is Vnano?
* How do I embed Vnano into my application?

And other related queries.

Good lack!

---

\- Credits and Trademarks -

- ChatGPT is a trademark or a registered trademark of OpenAI OpCo, LLC in the United States and other countries.

- Other names may be either a registered trademarks or trademarks of their respective owners.

