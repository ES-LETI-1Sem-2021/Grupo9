package es.grupo9;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Action;
import org.trello4j.model.Card;
import org.trello4j.model.Member;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TrelloManager class. Uses the Trello API ot get all the necessary information.
 */
public class TrelloManager {

    private static Trello trello;
    private static String boardId;

    /**
     * Constructor function, creates an instance of TrelloManager.
     *
     * @param API_KEY  User's API Trello Key.
     * @param TOKEN    User's Trello TOKEN.
     * @param BOARD_ID Trello Board ID.
     */
    public TrelloManager(String API_KEY, String TOKEN, String BOARD_ID) {
        boardId = BOARD_ID;
        trello = new TrelloImpl(API_KEY, TOKEN);
    }

    /**
     * Gets the ID of cards from the Backlog pertaining to a specific Sprint. This method gets the "Done"
     * list of each sprint and returns a list of the cards on it.
     *
     * @param sprintNumber Sprint the user wants the cards from.
     * @return A list of cards from the desired Sprint.
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public List<Card> getFinishedSprintBacklog(int sprintNumber) throws IOException {
        // Get the list of cards from the board
        List<Card> cards = trello.getCardsByList(getSprintListByName(sprintNumber, "Done"));

        // Returns the cards from the Done list of the sprint asked
        return new ArrayList<>(cards);
    }

    /**
     * Returns the number of hours worked and estimated of a given card.
     *
     * @param cardID ID of the card.
     * @return Double[] with the following format: [HOURS WORKED, HOURS ESTIMATED].
     */
    public Double[] getCardHours(String cardID) {
        List<Action> comments = trello.getActionsByCard(cardID);
        comments.removeIf(action -> action.getData().getText() == null); // removing null comments
        Double[] real = new Double[comments.size()];
        Double[] estimate = new Double[comments.size()];

        int aux = 0;

        while (aux != comments.size()) {
            for (Action action : comments) {
                if (action.getData().getText().contains("plus!")) {
                    // Normal structure of a comment with plus! = "plus! @NAME #/#"
                    // First split = [plus! @NAME #, #]
                    String[] firstSplit = action.getData().getText().split("/");

                    // Second split = [plus!, @NAME, #]
                    String[] secondSplit = firstSplit[0].split(" ");

                    // Get the last element of the array (hours)
                    real[aux] = Double.valueOf(secondSplit[secondSplit.length - 1]);
                    estimate[aux] = Double.valueOf(firstSplit[firstSplit.length - 1]);

                }
                aux++;
            }
        }

        Double[] hours = new Double[2];
        hours[0] = Utils.getSum(real);
        hours[1] = Utils.getSum(estimate);


        return hours;
    }

    /**
     * Returns a Double array with the number of hours worked, hours estimated and the cost of the hours worked of a given member.
     * This method works by iterating through the "Increment" list of the SPRINT requested and deleting the cards that don't
     * have the member requested on them.
     *
     * @param sprintNumber number of the SPRINT.
     * @param memberName   name of the member.
     * @return Double[] with the following format: [HOURS WORKED, HOURS ESTIMATED, COST OF HOURS WORKED].
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public Double[] getSprintHoursByMember(String memberName, int sprintNumber) throws IOException {
        List<Card> memberSprintList = trello.getCardsByList(getSprintListByName(sprintNumber, "Done"));

        // Removing cards without the member
        String memberId = getMemberIdByName(memberName);
        memberSprintList.removeIf(card -> !(card.getIdMembers().contains(memberId)));

        Double real = 0.0;
        Double estimate = 0.0;

        // Sum up hours worked on each card
        for (Card card : memberSprintList) {
            Double[] aux = getCardHours(card.getId());
            real += aux[0];
            estimate += aux[1];
        }

        return new Double[]{real, estimate, Utils.getCost(real)};
    }

    /**
     * Returns an array with the amount of committed activities and the total hours worked on those activities by member,
     * as well as the cost. This method works by iterating every "Increment" list of the given SPRINT and removing the cards
     * without the requested member on them.
     *
     * @param memberName   name of the member.
     * @param sprintNumber number of the sprint.
     * @return Double[] with the following format [NUMBER OF ACTIVITIES, TOTAL HOURS WORKED, COST OF HOURS WORKED].
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public Double[] getCommittedActivitiesByMember(String memberName, int sprintNumber) throws IOException {
        Double[] activities = new Double[3];
        double totalHours = 0.0;
        List<Card> memberSprintList = trello.getCardsByList(getSprintListByName(sprintNumber, "Done"));

        // Removing cards without the member
        String memberId = getMemberIdByName(memberName);
        memberSprintList.removeIf(card -> !(card.getIdMembers().contains(memberId)));

        List<Card> activitiesCount = new ArrayList<>(memberSprintList);
        totalHours += getSprintHoursByMember(memberName, sprintNumber)[0];

        activities[0] = (double) activitiesCount.size();
        activities[1] = totalHours;
        activities[2] = Utils.getCost(totalHours);

        return activities;
    }

    /**
     * Returns an array with the amount of committed activities and the total hours worked on those activities by member,
     * as well as the cost. This method works by iterating every "Meetings" list of the given SPRINT and removing the cards
     * without the requested member on them.
     *
     * @param memberName   name of the member.
     * @param sprintNumber number of the SPRINT.
     * @return Double[] with the following format [NUMBER OF ACTIVITIES, TOTAL HOURS WORKED, COST OF HOURS WORKED].
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public Double[] getNotCommittedActivitiesByMember(String memberName, int sprintNumber) throws IOException {
        Double[] activities = new Double[3];
        double totalHours = 0.0;
        List<Card> memberMeetingList = trello.getCardsByList(getSprintListByName(sprintNumber, "Meetings"));

        // Removing cards without the member
        String memberId = getMemberIdByName(memberName);
        memberMeetingList.removeIf(card -> !(card.getIdMembers().contains(memberId)));

        List<Card> activitiesCount = new ArrayList<>(memberMeetingList);
        for (Card card : memberMeetingList) {
            totalHours += getCardHours(card.getId())[0];
        }


        activities[0] = (double) activitiesCount.size();
        activities[1] = totalHours;
        activities[2] = Utils.getCost(totalHours);

        return activities;
    }

    /**
     * Returns a list with all the Meetings of a given SPRINT.
     *
     * @param sprintNumber number of the SPRINT.
     * @return A list of cards (meetings) of the SPRINT requested.
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public List<Card> getMeetings(int sprintNumber) throws IOException {
        return trello.getCardsByList(getSprintListByName(sprintNumber, "Meetings"));
    }

    /**
     * Returns the ID of a Board List provided its name and Sprint number.
     *
     * @param sprintNumber number of the Sprint.
     * @param listName     name of the Board the user wants the ID from.
     * @return String ID of the Board List.
     * @throws IOException if list isn't part of the board.
     */
    private String getSprintListByName(int sprintNumber, String listName) throws IOException {
        List<org.trello4j.model.List> boardLists = trello.getListByBoard(boardId);
        for (org.trello4j.model.List boardList : boardLists) {
            if (boardList.getName().contains("Sprint " + sprintNumber) && boardList.getName().contains(listName)) {
                return boardList.getId();
            }
        }
        throw new IOException("List does not exist in this board.");
    }

    /**
     * Returns the ID of a Board List provided its name.
     *
     * @param listName name of the Board the user wants the ID from.
     * @return String ID of the Board List.
     * @throws IOException if list isn't part of the board.
     */
    private String getBoardListIdByName(String listName) throws IOException {
        List<org.trello4j.model.List> boardLists = trello.getListByBoard(boardId);
        for (org.trello4j.model.List boardList : boardLists) {
            if (boardList.getName().contains(listName)) {
                return boardList.getId();
            }
        }
        throw new IOException("List does not exist in this board.");
    }

    /**
     * Returns the ID of a Member provided their name.
     *
     * @param memberName name of the Member the user wants the ID from.
     * @return String ID of the Member.
     * @throws IOException if member isn't part of the board.
     */
    public String getMemberIdByName(String memberName) throws IOException {
        List<Member> memberList = trello.getMembersByBoard(boardId);
        for (Member member : memberList) {
            if (member.getFullName().contains(memberName)) {
                return member.getId();
            }
        }
        throw new IOException("Member does not exist in this board.");
    }

    /**
     * Gets the number of current Sprints.
     *
     * @return int number of Sprints so far.
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public int getSprintCount() throws IOException {
        // Get "Sprints" list cards
        List<Card> cards = new ArrayList<>(trello.getCardsByList(getBoardListIdByName("Sprints")));

        return cards.size();
    }

    /**
     * Returns the number of members on the board.
     * @return int Number of members on the board.
     */
    public int getMemberCount() {
        List<Member> memberList = trello.getMembersByBoard(boardId);
        return memberList.size();
    }

    /**
     * Returns the members on the board.
     * @return List of members on the board.
     */
    public List<Member> getMembers() {
        return trello.getMembersByBoard(boardId);
    }

    /**
     * Returns the beginning and end date of each Sprint.
     *
     * @param sprintNumber number of the Sprint.
     * @return String beginning and end date of each Sprint.
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public String getSprintDate(int sprintNumber) throws IOException {
        String date = "";

        for (Card sprint : trello.getCardsByList(getBoardListIdByName("Sprints"))) {
            if (sprint.getName().contains(String.valueOf(sprintNumber))) {
                date = sprint.getDesc();
            }
        }

        return date;
    }

    /**
     * Gets the project name (title of the board).
     *
     * @return String project name.
     */
    public String getProjectName() {
        return trello.getBoard(boardId).getName();
    }

    /**
     * Returns the beginning date of the project. Works by getting the beginning date of the first Sprint.
     *
     * @return String beginning date.
     * @throws IOException see {@link #getBoardListIdByName(String)};
     */
    public String getBeginningDate() throws IOException {
        return getSprintDate(1).split(":")[1].split("\n")[0];
    }
}