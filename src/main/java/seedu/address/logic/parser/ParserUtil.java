package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    /** Parses a positive persistent customer ID, rejecting values outside the supported integer range. */
    public static int parseCustomerId(String customerId) throws ParseException {
        requireNonNull(customerId);
        String trimmedId = customerId.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedId)) {
            throw new ParseException("Please provide a valid positive customer ID");
        }
        try {
            return Integer.parseInt(trimmedId);
        } catch (NumberFormatException e) {
            throw new ParseException("Please provide a valid positive customer ID", e);
        }
    }

    /**
     * Parses a string into a {@code DogName}.
     * Leading and trailing whitespace is trimmed.
     *
     * @throws ParseException if the given name is invalid
     */

    public static DogName parseDogName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!DogName.isValidName(trimmedName)) {
            throw new ParseException(DogName.MESSAGE_CONSTRAINTS);
        }
        return new DogName(trimmedName);
    }

    /**
     * Parses a string into a {@code Breed}.
     * Leading and trailing whitespace is trimmed.
     *
     * @throws ParseException if the given breed is invalid
     */
    public static Breed parseBreed(String breed) throws ParseException {
        requireNonNull(breed);
        String trimmedBreed = breed.trim();
        if (!Breed.isValidBreed(trimmedBreed)) {
            throw new ParseException(Breed.MESSAGE_CONSTRAINTS);
        }
        return new Breed(trimmedBreed);
    }

    /**
     * Parses a string into a {@code Size}.
     * Leading and trailing whitespace is trimmed.
     *
     * @throws ParseException if the given size is invalid
     */
    public static Size parseSize(String size) throws ParseException {
        requireNonNull(size);
        String trimmedSize = size.trim();
        if (!Size.isValidSize(trimmedSize)) {
            throw new ParseException(Size.MESSAGE_CONSTRAINTS);
        }
        return new Size(trimmedSize);
    }

    //==========================CUSTOMER RELATED====================================
    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static seedu.address.model.customer.Name parseCustomerName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!seedu.address.model.customer.Name.isValidName(trimmedName)) {
            throw new ParseException(seedu.address.model.customer.Name.MESSAGE_CONSTRAINTS);
        }
        return new seedu.address.model.customer.Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static seedu.address.model.customer.Phone parseCustomerPhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!seedu.address.model.customer.Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(seedu.address.model.customer.Phone.MESSAGE_CONSTRAINTS);
        }
        return new seedu.address.model.customer.Phone(trimmedPhone);
    }
    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static seedu.address.model.customer.Email parseCustomerEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!seedu.address.model.customer.Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(seedu.address.model.customer.Email.MESSAGE_CONSTRAINTS);
        }
        return new seedu.address.model.customer.Email(trimmedEmail);
    }

    /**
     * Parses a {@code String backupcontact} into a {@code BackupContact}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static BackupContact parseCustomerBackupContact(String backupContact) throws ParseException {
        requireNonNull(backupContact);
        String trimmedPhone = backupContact.trim();
        if (!BackupContact.isValidBackupContact(trimmedPhone)) {
            throw new ParseException(BackupContact.MESSAGE_CONSTRAINTS);
        }
        return new BackupContact(trimmedPhone);
    }

    //==========================ACTIVITY RELATED====================================
    /** Parses a nonblank activity name. */
    public static ActivityName parseActivityName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!ActivityName.isValidName(trimmedName)) {
            throw new ParseException(ActivityName.MESSAGE_CONSTRAINTS);
        }
        return new ActivityName(trimmedName);
    }




}
