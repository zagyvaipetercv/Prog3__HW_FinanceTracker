package financetracker.controllers;

import java.util.ArrayList;
import java.util.List;

import financetracker.datatypes.Category;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.modelserailizer.IdNotFoundException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.windowing.MainFrame;

public class CategoryController extends Controller<Category> {
    private static final String DEFAULT_SAVE_PATH = "saves/categories.dat";

    public CategoryController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public CategoryController(String savePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(savePath, mainFrame);
    }

    /**
     * Returns the id of a category with a specified name.
     * 
     * @param categoryName name of the category
     * @return the id of the category
     * @throws CategoryLookupFailedException if an IO Error occured
     * @throws IdNotFoundException if the category with the name does not exist
     */
    public long getCategoryId(String categoryName) throws CategoryLookupFailedException, IdNotFoundException {
        try {
            return modelSerializer.findId(
                    new Category(-1, categoryName),
                    (c1, c2) -> (c1.getName().compareTo(c2.getName())));
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to find id for cotegory");
        }
    }

    /**
     * Returns a category with a specified name.
     * 
     * @param categoryName name of the category
     * @return The category with the specified name
     * @throws CategoryLookupFailedException if an IO Error occured or the category with the name was not found
     */
    public Category getCategory(String categoryName) throws CategoryLookupFailedException {
        try {
            for (Category category : modelSerializer.readAll()) {
                if (category.getName().equals(categoryName)) {
                    return category;
                }
            }
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to find cateogry due to an IO Error");
        }

        throw new CategoryLookupFailedException("Category with name of '" + categoryName + "' was not found");
    }

    /**
     * Returns a category with a specified id
     * 
     * @param id the id of the category
     * @return the category with the id
     * @throws CategoryLookupFailedException if an IO Error occured or the category with the name was not found
     */
    public Category getCategory(long id) throws CategoryLookupFailedException {
        try {
            for (Category category : modelSerializer.readAll()) {
                if (category.getId() == id) {
                    return category;
                }
            }
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to find category due to an IO Error");
        }

        throw new CategoryLookupFailedException("Category with id of '" + id + "' was not found");
    }

    /**
     * Returns every saved category's name.
     * 
     * @return A list of strings containing every saved category name
     * @throws CategoryLookupFailedException if an IO Error occured
     */
    public List<String> getCategoryNames() throws CategoryLookupFailedException {
        try {
            List<Category> allCategories = modelSerializer.readAll();
            List<String> categoryNames = new ArrayList<>();

            for (Category category : allCategories) {
                categoryNames.add(category.getName());
            }

            return categoryNames;
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to lookup categories");
        }
    }

    /**
     * Creates a category with the name specified in the parameters.
     * 
     * @param categoryName the name of the new category
     * @return the category created in th process
     * @throws CreatingRecordFailed if the creating the category failed due to an IO Error
     */
    public Category createCategory(String categoryName) throws CreatingRecordFailed {
        Category result = new Category(modelSerializer.getNextId(), categoryName.toUpperCase());
        try {
            modelSerializer.appendNewData(result);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingRecordFailed("Failed to create new category due to IO Error", result);
        }

        return result;
    }
}
