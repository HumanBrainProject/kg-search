export const help = `#How to use the Knowledge Graph Search

The Knowledge Graph Search allows you to find and/or to explore the data which is registered with, and curated by the Human Brain Project. You can execute your search in different ways:

##Full-text search
As in any other search engine, you have the possibility to search by a specified term. For this, you can enter anything you're looking for in the search bar (e.g. \`brain\`) and the corresponding results will appear in the results list.

###Typo-correction
Please note, that the search also tries to find results if you have entered typos: You could e.g. write \`crain\` and the search would still return results for \`brain\`. 

###Multi-term queries
If you provide multiple terms, the search will treat them as **OR** combinations: if you're looking for \`brain neuroscience\`, results will be provided which contain either \`brain\` or \`neuroscience\` or both. If you rather want to restrict the results to items which contain both words, you can add an **AND** (lower- or uppercase) between your terms. Your query would be \`brain and neuroscience\`.

###Advanced queries
For more advanced queries, the Knowledge Graph Search supports the [Apache Lucene Syntax](http://lucene.apache.org/core/2_9_4/queryparsersyntax.html) which you can use inside the search bar.

##Search by type
The Knowledge Graph Search allows you to find different types of results. If you're e.g. searching by a person's name, your search results will by default contain the person, as well as datasets the person has contributed to as search results. If you already know for what type of result you're looking for, you can restrict your search by clicking on the type selection buttons below the search bar. Please note that this selection list will adapt to your full-text search. If the results of your full-text search don't contain persons, this type will not show up in the selection list anymore.

###Faceted filters
As soon as you have selected a type, you will see a faceted filter panel apear on the left. Faceted filters can be used for restricting your results based on some specific properties of the result sets. The provided filters depend on the type you've chosen, so they will be different if you have chosen persons or datasets.

##Navigate through the result cards
After selecting one of the presented results, you will be able to see all the details of this item on a result card. Additionally, you will see links to other result cards (e.g. to the contributors of a dataset). If you select one of these links, the result card of the chosen person will be shown including all details like e.g. other datasets the person has contributed to. This allows you to choose any starting point from the search to navigate through and explore the EBRAINS Knowledge Graph. If you would like to return to your original starting point, you can do so by selecting the **Previous** button on the top left corner of the result card.

##Save or share your queries
If you want to save or share your queries, you can simply reuse the URL in your browser by either bookmarking the current state in your web browser or by using the share buttons (on the bottom right in the main screen, or on the top right in the result card) which allow you to either copy the URL into your clipboard or to generate an e-mail to send the link out to anyone who could be interested in what you've found.

##HBP member access
If you own an HBP account, you might be eligible to see and search additional data (depending on your access rights). If so, you can select the "Log in" button on the bottom right of the page. After the login, choose as member of which group you want to browse the Knowledge Graph in the selection box which shows up next to the "Log out" button. If no selection box shows up, this means that you aren't member of one of the extended Knowledge Graph Search spaces. 

##Further questions and/or feedback
If you have any further questions and/or feedback, do not hesitate to [contact us by e-mail](mailto:kg@ebrains.eu)

##Acknowledgements
This open source software code was developed in part or in whole in the Human Brain Project, funded from the European Union’s Horizon 2020 Framework Programme for Research and Innovation under Specific Grant Agreements No. 720270 and No. 785907 (Human Brain Project SGA1 and SGA2).

Powered by [Blue Brain Nexus](https://bluebrainnexus.io/docs/) `;
