package es.grupo9;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
// import org.trello4j.model.Board;
import org.trello4j.model.Action;
import org.trello4j.model.Card;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class TrelloManager{

    private static Trello trello;
//  private static Board board;

    private static String boardId;

    public TrelloManager(String API_KEY, String TOKEN, String BOARD_ID) {

        boardId = BOARD_ID;
        trello = new TrelloImpl(API_KEY, TOKEN);
  //    Board board = trello.getBoard(boardId);
    }

    /**
     * Gets the ID of cards from the Backlog pertaining to a specific Sprint. Each Increment list has
     * a #SPRINT(NUMBER) on its label. This method iterates over the board lists until it finds one
     * related to the sprint wanted.
     *
     * @param sprintNumber Sprint the user wants the cards from.
     * @return List<Card> list of cards from the desired Sprint.
     */
    public static List<Card> getFinishedSprintBacklog(int sprintNumber) {
        // Initialize auxiliary variables

        // Get the list of cards from the board
        List<Card> cards = trello.getCardsByList(getBoardListIdByName("#SPRINT" +  sprintNumber + " - Increment"));

        // Returns the cards from the Done list of the sprint asked
        return new ArrayList<>(cards);
    }

    /**
     * Gets the amount of hours worked on a card, provided its ID.
     * @param cardID ID of the Card.
     * @return Double hours worked on a given card.
     */
    public static Double getCardHours(String cardID){
        List<Action> comments = trello.getActionsByCard(cardID);
        comments.removeIf(action -> action.getData().getText() == null); // removing null comments

        // Format to only have 2 decimal places
        DecimalFormat df = new DecimalFormat("#.##");

        Double[] hours = new Double[comments.size()];

        int aux = 0;

        while(aux != comments.size()) {
            for (Action action : comments) {
                if (action.getData().getText().contains("plus!")) {
                    // Normal structure of a comment with plus! = "plus! @NAME #/#"
                    // First split = [plus! @NAME #, #]
                    String[] firstSplit = action.getData().getText().split("/");

                    // Second split = [plus!, @NAME, #]
                    String[] secondSplit = firstSplit[0].split(" ");

                    // Get the last element of the array (hours)
                    hours[aux] = Double.valueOf(secondSplit[secondSplit.length - 1]);
                }
                aux++;
            }
        }

        Double sum = Utils.getSum(hours);

        return Double.valueOf(df.format(sum));
    }

    /**
     *
     * @param sprintNumber number of the Sprint the cost will be calculated for.
     */
    public static Double getSprintCost(int sprintNumber) {
        List<Card> sprintList = trello.getCardsByList(getBoardListIdByName("#SPRINT" + sprintNumber + " - Increment"));

        // Format to only have 2 decimal places
        DecimalFormat df = new DecimalFormat("#.##");

        // Sum up hours worked on each card
        Double hours = getTotalHours(sprintList);

        Double cost = Utils.getCost(hours);

        return Double.valueOf(df.format(cost));
    }

    /**
     * Gets the total amount of hours (sum of hours on each card) of a given list of cards.
     * @param sprintList list of all the cards in that Sprint.
     * @return Double sum of all the hours.
     */
    private static Double getTotalHours(List<Card> sprintList) {
        double totalHours = 0.0;

        for (Card card : sprintList) totalHours += getCardHours(card.getId());

        return totalHours;
    }


    /**
     * Returns the ID of a Board List provided its name.
     * @param listName name of the Board the user wants the ID from.
     * @return String ID of the Board List.
     */
    public static String getBoardListIdByName(String listName){
        String listId = "";
        List<org.trello4j.model.List> boardLists = trello.getListByBoard(boardId);
        for(org.trello4j.model.List boardList: boardLists) {
            if (boardList.getName().contains(listName)) {
                listId = boardList.getId();
            }
        }
        return listId;
    }

    /**
     * Returns a HashMap with all the cards of the meetings in each Sprint.
     * @return HashMap<Integer, List<Card>> HashMap with the Sprint Number and list of cards (meetings) from that sprint.
     */
    public static HashMap<Integer, List<Card>> getMeetings(){
        HashMap<Integer, List<Card>> meetings = new HashMap<>();

        // Get all the Meetings cards
        for(int sprintNumber = 1; sprintNumber < getSprintCount() + 1; sprintNumber++){
            meetings.put(sprintNumber, trello.getCardsByList(getBoardListIdByName("#SPRINT" + sprintNumber + " - Meetings")));
        }

        return meetings;
    }

    /**
     * Gets the number of current Sprints.
     * @return int number of Sprints so far.
     */
    public static int getSprintCount(){
        // Get "Sprints" list cards
        List<Card> cards = new ArrayList<>(trello.getCardsByList(getBoardListIdByName("Sprints")));

        return cards.size();
    }

}